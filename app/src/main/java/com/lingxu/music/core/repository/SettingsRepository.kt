package com.lingxu.music.core.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * 应用设置仓库。key 命名对齐原版 LX.AppSetting（common.* / player.* / playDetail.* / search.* / list.*）
 * 便于与原版配置互相参照与后续同步备份。
 */
class SettingsRepository(private val context: Context) {

    companion object {
        // common
        val KEY_API_SOURCE = stringPreferencesKey("common.apiSource")
        val KEY_THEME = stringPreferencesKey("common.theme")
        val KEY_LANG = stringPreferencesKey("common.langId")
        val KEY_FONT_SCALE = floatPreferencesKey("common.fontScale")
        val KEY_SHARE_TYPE = stringPreferencesKey("common.shareType")
        val KEY_AUTO_HIDE_PLAY_BAR = booleanPreferencesKey("common.autoHidePlayBar")
        // player
        val KEY_QUALITY = stringPreferencesKey("player.quality")
        val KEY_PLAY_MODE = stringPreferencesKey("player.playMode")
        val KEY_IS_SHOW_LYRIC_TRANSLATION = booleanPreferencesKey("player.isShowLyricTranslation")
        val KEY_IS_SHOW_LYRIC_ROMA = booleanPreferencesKey("player.isShowLyricRoma")
        val KEY_STARTUP_AUTO_PLAY = booleanPreferencesKey("player.startupAutoPlay")
        val KEY_IS_SAVE_PLAY_TIME = booleanPreferencesKey("player.isSavePlayTime")
        val KEY_IS_HANDLE_AUDIO_FOCUS = booleanPreferencesKey("player.isHandleAudioFocus")
        val KEY_PLAYBACK_RATE = floatPreferencesKey("player.playbackRate")
        val KEY_VOLUME = floatPreferencesKey("player.volume")
        // 定时退出（对应原版 player.timeoutExit / timeoutExitPlayed）
        val KEY_TIMEOUT_EXIT_MINUTES = androidx.datastore.preferences.core.intPreferencesKey("player.timeoutExit")
        val KEY_TIMEOUT_EXIT_PLAYED = booleanPreferencesKey("player.timeoutExitPlayed")
        // playDetail
        val KEY_LYRIC_ALIGN = stringPreferencesKey("playDetail.style.align")
        val KEY_LYRIC_FONT_SIZE = floatPreferencesKey("playDetail.style.lrcFontSize")
        val KEY_IS_ZOOM_ACTIVE_LRC = booleanPreferencesKey("playDetail.isZoomActiveLrc")
        // search
        val KEY_IS_SHOW_HOT_SEARCH = booleanPreferencesKey("search.isShowHotSearch")
        val KEY_IS_SHOW_HISTORY_SEARCH = booleanPreferencesKey("search.isShowHistorySearch")
        // list
        val KEY_IS_SHOW_SOURCE = booleanPreferencesKey("list.isShowSource")
        val KEY_IS_SHOW_ALBUM_NAME = booleanPreferencesKey("list.isShowAlbumName")
        val KEY_IS_SHOW_INTERVAL = booleanPreferencesKey("list.isShowInterval")
        val KEY_ADD_MUSIC_LOCATION = stringPreferencesKey("list.addMusicLocationType")
        // glass（本项目自有）
        val KEY_BLUR_ENABLED = booleanPreferencesKey("glass.blurEnabled")
        val KEY_BLUR_RADIUS = floatPreferencesKey("glass.blurRadius")
        // userApi
        val KEY_ENABLE_USER_API_LOG = booleanPreferencesKey("userApi.log")

        const val LANG_SYSTEM = "system"
        const val SHARE_SYSTEM = "system"
        const val ALIGN_LEFT = "left"
        const val ADD_LOCATION_TOP = "top"
        const val ADD_LOCATION_BOTTOM = "bottom"
        /** 定时退出：0 表示未开启 */
        const val TIMEOUT_EXIT_OFF = 0
    }

    // ================= common =================

    val apiSource: Flow<String> = context.dataStore.data.map { it[KEY_API_SOURCE] ?: "kw" }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "system" }
    val lang: Flow<String> = context.dataStore.data.map { it[KEY_LANG] ?: LANG_SYSTEM }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: 1f }
    val shareType: Flow<String> = context.dataStore.data.map { it[KEY_SHARE_TYPE] ?: SHARE_SYSTEM }
    val autoHidePlayBar: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_HIDE_PLAY_BAR] ?: true }

    // ================= player =================

    val quality: Flow<String> = context.dataStore.data.map { it[KEY_QUALITY] ?: "128k" }
    val playMode: Flow<String> = context.dataStore.data.map { it[KEY_PLAY_MODE] ?: "listLoop" }
    val showLyricTranslation: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_LYRIC_TRANSLATION] ?: true }
    val showLyricRoma: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_LYRIC_ROMA] ?: false }
    val startupAutoPlay: Flow<Boolean> = context.dataStore.data.map { it[KEY_STARTUP_AUTO_PLAY] ?: false }
    val isSavePlayTime: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SAVE_PLAY_TIME] ?: false }
    val isHandleAudioFocus: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_HANDLE_AUDIO_FOCUS] ?: true }
    val playbackRate: Flow<Float> = context.dataStore.data.map { it[KEY_PLAYBACK_RATE] ?: 1f }
    val volume: Flow<Float> = context.dataStore.data.map { it[KEY_VOLUME] ?: 1f }
    val timeoutExitMinutes: Flow<Int> = context.dataStore.data.map { it[KEY_TIMEOUT_EXIT_MINUTES] ?: 0 }
    val timeoutExitPlayed: Flow<Boolean> = context.dataStore.data.map { it[KEY_TIMEOUT_EXIT_PLAYED] ?: true }

    // ================= playDetail =================

    val lyricAlign: Flow<String> = context.dataStore.data.map { it[KEY_LYRIC_ALIGN] ?: ALIGN_LEFT }
    val lyricFontSize: Flow<Float> = context.dataStore.data.map { it[KEY_LYRIC_FONT_SIZE] ?: 1f }
    val isZoomActiveLrc: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_ZOOM_ACTIVE_LRC] ?: false }

    // ================= search =================

    val isShowHotSearch: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_HOT_SEARCH] ?: true }
    val isShowHistorySearch: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_HISTORY_SEARCH] ?: true }

    // ================= list =================

    val isShowSource: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_SOURCE] ?: true }
    val isShowAlbumName: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_ALBUM_NAME] ?: false }
    val isShowInterval: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_INTERVAL] ?: true }
    val addMusicLocation: Flow<String> = context.dataStore.data.map { it[KEY_ADD_MUSIC_LOCATION] ?: ADD_LOCATION_BOTTOM }

    // ================= glass =================

    val blurEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLUR_ENABLED] ?: true }
    val blurRadius: Flow<Float> = context.dataStore.data.map { prefs ->
        // 兼容旧版本把 blurRadius 以字符串存放的格式
        (prefs[KEY_BLUR_RADIUS] ?: prefs[stringPreferencesKey("glass.blurRadius")]?.toFloatOrNull() ?: 24f)
    }

    // ================= userApi =================

    val enableUserApiLog: Flow<Boolean> = context.dataStore.data.map { it[KEY_ENABLE_USER_API_LOG] ?: false }

    suspend fun setApiSource(value: String) = context.dataStore.edit { it[KEY_API_SOURCE] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[KEY_THEME] = value }
    suspend fun setLang(value: String) = context.dataStore.edit { it[KEY_LANG] = value }
    suspend fun setFontScale(value: Float) = context.dataStore.edit { it[KEY_FONT_SCALE] = value }
    suspend fun setShareType(value: String) = context.dataStore.edit { it[KEY_SHARE_TYPE] = value }
    suspend fun setAutoHidePlayBar(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_HIDE_PLAY_BAR] = value }

    suspend fun setQuality(value: String) = context.dataStore.edit { it[KEY_QUALITY] = value }
    suspend fun setPlayMode(value: String) = context.dataStore.edit { it[KEY_PLAY_MODE] = value }
    suspend fun setShowLyricTranslation(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_LYRIC_TRANSLATION] = value }
    suspend fun setShowLyricRoma(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_LYRIC_ROMA] = value }
    suspend fun setStartupAutoPlay(value: Boolean) = context.dataStore.edit { it[KEY_STARTUP_AUTO_PLAY] = value }
    suspend fun setIsSavePlayTime(value: Boolean) = context.dataStore.edit { it[KEY_IS_SAVE_PLAY_TIME] = value }
    suspend fun setIsHandleAudioFocus(value: Boolean) = context.dataStore.edit { it[KEY_IS_HANDLE_AUDIO_FOCUS] = value }
    suspend fun setPlaybackRate(value: Float) = context.dataStore.edit { it[KEY_PLAYBACK_RATE] = value }
    suspend fun setVolume(value: Float) = context.dataStore.edit { it[KEY_VOLUME] = value }
    suspend fun setTimeoutExitMinutes(value: Int) = context.dataStore.edit { it[KEY_TIMEOUT_EXIT_MINUTES] = value }
    suspend fun setTimeoutExitPlayed(value: Boolean) = context.dataStore.edit { it[KEY_TIMEOUT_EXIT_PLAYED] = value }

    suspend fun setLyricAlign(value: String) = context.dataStore.edit { it[KEY_LYRIC_ALIGN] = value }
    suspend fun setLyricFontSize(value: Float) = context.dataStore.edit { it[KEY_LYRIC_FONT_SIZE] = value }
    suspend fun setIsZoomActiveLrc(value: Boolean) = context.dataStore.edit { it[KEY_IS_ZOOM_ACTIVE_LRC] = value }

    suspend fun setIsShowHotSearch(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_HOT_SEARCH] = value }
    suspend fun setIsShowHistorySearch(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_HISTORY_SEARCH] = value }

    suspend fun setIsShowSource(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_SOURCE] = value }
    suspend fun setIsShowAlbumName(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_ALBUM_NAME] = value }
    suspend fun setIsShowInterval(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_INTERVAL] = value }
    suspend fun setAddMusicLocation(value: String) = context.dataStore.edit { it[KEY_ADD_MUSIC_LOCATION] = value }

    suspend fun setBlurEnabled(value: Boolean) = context.dataStore.edit { it[KEY_BLUR_ENABLED] = value }
    suspend fun setBlurRadius(value: Float) = context.dataStore.edit { it[KEY_BLUR_RADIUS] = value }
    suspend fun setEnableUserApiLog(value: Boolean) = context.dataStore.edit { it[KEY_ENABLE_USER_API_LOG] = value }

    suspend fun currentApiSource(): String = apiSource.first()
    suspend fun currentQuality(): String = quality.first()
    suspend fun currentLang(): String = lang.first()
}
