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
import com.lingxu.music.core.repository.SettingsRepository
import com.lingxu.music.core.repository.ShareRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    lateinit var shareRepository: ShareRepository
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
        // 备份是直接写 db 的，不刷新内存缓存的话下一次写操作会把刚恢复的数据覆盖掉
        backupRepository.onImported = {
            favoritesRepository.reload()
            myListsRepository.reload()
        }
        shareRepository = ShareRepository(this)
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
        playerRepository.playInfoLoader = { musicInfo ->
            val url = runCatching { musicRepository.getMusicUrl(musicInfo) }.getOrNull()
            if (url != null) musicInfo.copy(meta = musicInfo.meta.copy(filePath = url)) else musicInfo
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
            // 恢复播放模式（原版 player.togglePlayMethod）
            playerRepository.restorePlayMode(settings.playMode.first())
            // 恢复上次播放的队列；startupAutoPlay 决定是否直接开播
            val lastPlay = database.loadLastPlay()
            if (lastPlay != null) {
                // 设置里的「保存播放进度」开关此前完全无效：恢复时总是从头播
                val savePlayTime = runCatching { settings.isSavePlayTime.first() }.getOrDefault(false)
                playerRepository.restoreLastPlay(
                    lastPlay,
                    settings.startupAutoPlay.first(),
                    if (savePlayTime) lastPlay.positionMs else 0L,
                )
            }
            // 音频焦点处理开关
            playerRepository.setHandleAudioFocus(settings.isHandleAudioFocus.first())
            appScope.launch {
                settings.isHandleAudioFocus.collect { playerRepository.setHandleAudioFocus(it) }
            }
            // 倍速与音量
            playerRepository.setPlaybackRate(settings.playbackRate.first())
            playerRepository.setVolume(settings.volume.first())
            appScope.launch {
                settings.playbackRate.collect { playerRepository.setPlaybackRate(it) }
            }
            appScope.launch {
                settings.volume.collect { playerRepository.setVolume(it) }
            }
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
