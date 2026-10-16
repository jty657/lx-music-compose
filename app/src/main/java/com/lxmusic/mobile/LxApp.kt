package com.lxmusic.mobile

import android.app.Application
import com.lxmusic.mobile.core.db.AppDatabase
import com.lxmusic.mobile.core.engine.BuiltinSdkBridge
import com.lxmusic.mobile.core.engine.ScriptEngineManager
import com.lxmusic.mobile.core.player.PlayerRepository
import com.lxmusic.mobile.core.repository.ApiSourceRepository
import com.lxmusic.mobile.core.repository.FavoritesRepository
import com.lxmusic.mobile.core.repository.MusicRepository
import com.lxmusic.mobile.core.repository.SettingsRepository
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
        engineManager = ScriptEngineManager(this)
        sdkBridge = BuiltinSdkBridge(this)
        apiSourceRepository = ApiSourceRepository(database)
        favoritesRepository = FavoritesRepository(database)
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

        // 初始化内置 SDK
        appScope.launch {
            val apiSource = settings.currentApiSource()
            sdkBridge.init(apiSource)
            musicRepository.setApiSource(apiSource)
            favoritesRepository.load()
            // 恢复播放模式（原版 player.togglePlayMethod）
            playerRepository.restorePlayMode(settings.playMode.first())
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
                engineManager.destroyScript()
            }
        }
    }

    private suspend fun loadUserApi(apiId: String) {
        val info = apiSourceRepository.getAll().find { it.id == apiId } ?: return
        val script = apiSourceRepository.getScript(apiId) ?: return
        engineManager.loadScript(info, script)
        // 引擎就绪后更新能力列表
        appScope.launch {
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
