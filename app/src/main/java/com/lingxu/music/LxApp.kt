package com.lingxu.music

import android.app.Application
import com.lingxu.music.core.db.AppDatabase
import com.lingxu.music.core.engine.BuiltinSdkBridge
import com.lingxu.music.core.engine.ScriptEngineManager
import com.lingxu.music.core.player.PlayerRepository
import com.lingxu.music.core.repository.ApiSourceRepository
import com.lingxu.music.core.repository.FavoritesRepository
import com.lingxu.music.core.repository.MyListsRepository
import com.lingxu.music.core.repository.MusicRepository
import com.lingxu.music.core.repository.BackupRepository
import com.lingxu.music.core.repository.DislikeRepository
import com.lingxu.music.core.repository.SettingsRepository
import com.lingxu.music.core.repository.ShareRepository
import com.lingxu.music.core.repository.UpdateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LxApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var settings: SettingsRepository
        private set
    lateinit var engineManager: ScriptEngineManager
        private set
    lateinit var sdkBridge: BuiltinSdkBridge
        private set
    lateinit var apiSourceRepository: ApiSourceRepository
        private set
    lateinit var favoritesRepository: FavoritesRepository
        private set
    lateinit var myListsRepository: MyListsRepository
        private set
    lateinit var backupRepository: BackupRepository
        private set
    lateinit var dislikeRepository: DislikeRepository
        private set
    lateinit var shareRepository: ShareRepository
        private set
    lateinit var updateRepository: UpdateRepository
        private set

    /** 当前音源能力收集协程（切换音源时重建，避免协程泄漏） */
    private var capabilitiesJob: kotlinx.coroutines.Job? = null
    lateinit var musicRepository: MusicRepository
        private set
    lateinit var playerRepository: PlayerRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.build(this)
        settings = SettingsRepository(this)
        // 封面等资源走 OkHttp 磁盘缓存（HttpClient 是单例，需要 App 注入 cacheDir）
        com.lingxu.music.core.request.HttpClient.instance.setCacheDir(cacheDir)
        engineManager = ScriptEngineManager(this)
        sdkBridge = BuiltinSdkBridge(this)
        apiSourceRepository = ApiSourceRepository(database)
        favoritesRepository = FavoritesRepository(database)
        myListsRepository = MyListsRepository(database)
        backupRepository = BackupRepository(this, database)
        dislikeRepository = DislikeRepository(this)
        // 备份是直接写 db 的，不刷新内存缓存的话下一次写操作会把刚恢复的数据覆盖掉
        backupRepository.onImported = {
            favoritesRepository.reload()
            myListsRepository.reload()
        }
        shareRepository = ShareRepository(this)
        updateRepository = UpdateRepository()
        musicRepository = MusicRepository(sdkBridge, engineManager, settings)
        playerRepository = PlayerRepository.get(this)
        // 注入歌词加载器（播放器切歌时自动拉歌词，含翻译）
        playerRepository.lyricLoader = { musicInfo ->
            musicRepository.getLyricInfo(musicInfo)
        }
        // 播放模式持久化
        playerRepository.onPlayModeChange = { mode ->
            appScope.launch { settings.setPlayMode(mode) }
        }
        // 注入封面加载器
        playerRepository.coverLoader = { musicInfo ->
            runCatching { musicRepository.getPicUrl(musicInfo) }.getOrNull()
        }
        playerRepository.lastPlaySaver = { state -> database.saveLastPlay(state) }
        // 不喜欢列表：命中则切歌时自动跳过
        playerRepository.dislikeMatcher = { music -> dislikeRepository.isDisliked(music) }
        playerRepository.playInfoLoader = { musicInfo ->
            val url = runCatching { musicRepository.getMusicUrl(musicInfo) }.getOrNull()
            if (url != null) musicInfo.copy(meta = musicInfo.meta.copy(filePath = url)) else musicInfo
        }

        // 设置项变化监听：拆成独立协程并加 catch 兜底。此前它们嵌在主初始化链里，
        // 一旦前面某处（如 DataStore 偏好文件损坏时的首读）抛异常，后面的监听全部不会注册，
        // 且表面看 App「正常启动」，问题很隐蔽。独立启动后各监听互不影响。
        appScope.launch {
            settings.enableUserApiLog.catch { }.collect { engineManager.scriptLogEnabled = it }
        }
        // 音源显示名类型（原名/别名）：同步到全局，供所有 displayName() 调用点读取。
        // DataStore 首读损坏时 catch 兜底，保持 SourceConst 默认 real，不影响启动。
        appScope.launch {
            settings.sourceNameType.catch { }.collect {
                com.lingxu.music.core.model.SourceConst.setNameType(it)
            }
        }
        appScope.launch {
            settings.isHandleAudioFocus.catch { }.collect { playerRepository.setHandleAudioFocus(it) }
        }
        appScope.launch {
            settings.playbackRate.catch { }.collect { playerRepository.setPlaybackRate(it) }
        }
        appScope.launch {
            settings.volume.catch { }.collect { playerRepository.setVolume(it) }
        }

        // 初始化内置 SDK
        appScope.launch {
            val apiSource = runCatching { settings.currentApiSource() }.getOrDefault("kw")
            sdkBridge.init(apiSource)
            musicRepository.setApiSource(apiSource)
            runCatching { favoritesRepository.load() }
            // 我的列表原先从未在冷启动时加载，导致「我的列表」页恒为空
            runCatching { myListsRepository.load() }
            // 同理：当前源是内置源时，音源页会显示「自定义音源 0/20」
            runCatching { apiSourceRepository.getAll() }
            runCatching { dislikeRepository.load() }
            // 恢复播放模式（原版 player.togglePlayMethod）
            runCatching { settings.playMode.first() }.getOrDefault("listLoop").let {
                playerRepository.restorePlayMode(it)
            }
            // 恢复上次播放的队列；startupAutoPlay 决定是否直接开播。
            // DataStore 偏好文件损坏时 first() 会抛异常，这里逐项兜底，避免整段恢复被跳过。
            val lastPlay = runCatching { database.loadLastPlay() }.getOrNull()
            if (lastPlay != null) {
                // 设置里的「保存播放进度」开关此前完全无效：恢复时总是从头播
                val savePlayTime = runCatching { settings.isSavePlayTime.first() }.getOrDefault(false)
                val autoPlay = runCatching { settings.startupAutoPlay.first() }.getOrDefault(false)
                playerRepository.restoreLastPlay(
                    lastPlay,
                    autoPlay,
                    if (savePlayTime) lastPlay.positionMs else 0L,
                )
            }
            // 定时停止播放冷启动恢复：原版只持久化分钟数、不存绝对到期时间，
            // 故采用「重启后按设定分钟数重新计时」的语义（不跨重启续算绝对时间），
            // 避免用户隔天打开 App 时定时器早已过期、或重新计时与预期不符。
            runCatching {
                val timeoutMinutes = settings.timeoutExitMinutes.first()
                if (timeoutMinutes > 0) {
                    playerRepository.startTimeoutExit(
                        timeoutMinutes,
                        settings.timeoutExitPlayed.first(),
                    )
                }
            }
            // 音频焦点 / 日志开关 / 倍速 / 音量的首值与变化监听已由上方独立协程统一处理，
            // 这里不再重复读取，避免损坏偏好首读抛异常中断后续的用户音源加载。
            // 如果是用户音源，加载脚本
            if (apiSource.startsWith("user_api")) {
                loadUserApi(apiSource)
            }
        }
    }

    /** 切换音源（对应原版 setApiSource） */
    fun setApiSource(apiId: String) {
        appScope.launch {
            settings.setApiSource(apiId)
            musicRepository.setApiSource(apiId)
            if (apiId.startsWith("user_api")) {
                loadUserApi(apiId)
            } else {
                capabilitiesJob?.cancel()
                engineManager.destroyScript()
            }
        }
    }

    private suspend fun loadUserApi(apiId: String) {
        val info = apiSourceRepository.getAll().find { it.id == apiId } ?: return
        val script = apiSourceRepository.getScript(apiId) ?: return
        engineManager.loadScript(info, script)
        capabilitiesJob?.cancel()
        capabilitiesJob = appScope.launch {
            engineManager.capabilities.collect { caps ->
                if (caps.isNotEmpty()) {
                    musicRepository.updateUserApiCapabilities(caps)
                }
            }
        }
    }

    companion object {
        lateinit var instance: LxApp
            private set

        val app: LxApp get() = instance
    }
}
