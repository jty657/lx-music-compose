package com.lingxu.music.ui.screens.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel。分组对齐原版 screens/Home/Views/Setting/settings：
 * Basic（基本）/ Player（播放）/ Lyric（歌词）/ Search（搜索）/ List（列表）/ Sync（同步备份）
 */
class SettingViewModel : ViewModel() {

    private val settings get() = LxApp.app.settings

    // 滑块每帧回调都要落盘，各设置项独立的节流时间戳
    private val throttleMarks = java.util.concurrent.ConcurrentHashMap<String, Long>()

    /** 距上次通过不足 intervalMs 则跳过；否则更新时间戳并返回 true */
    private fun throttle(key: String, intervalMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val last = throttleMarks.put(key, now) ?: 0L
        return now - last >= intervalMs
    }

    private fun clearThrottle(key: String) {
        throttleMarks.remove(key)
    }

    private fun <T> state(flow: kotlinx.coroutines.flow.Flow<T>, initial: T): StateFlow<T> =
        MutableStateFlow(initial).also { s ->
            viewModelScope.launch { flow.collect { s.value = it } }
        }

    // ============ 基本 ============
    val themeMode: StateFlow<String> = state(settings.theme, "system")
    val lang: StateFlow<String> = state(settings.lang, "system")
    val fontScale: StateFlow<Float> = state(settings.fontScale, 1f)
    val shareType: StateFlow<String> = state(settings.shareType, "system")
    val autoHidePlayBar: StateFlow<Boolean> = state(settings.autoHidePlayBar, true)

    // ============ 播放 ============
    val quality: StateFlow<String> = state(settings.quality, "128k")
    val playMode: StateFlow<String> = state(settings.playMode, "listLoop")
    val startupAutoPlay: StateFlow<Boolean> = state(settings.startupAutoPlay, false)
    val isSavePlayTime: StateFlow<Boolean> = state(settings.isSavePlayTime, false)
    val isHandleAudioFocus: StateFlow<Boolean> = state(settings.isHandleAudioFocus, true)

    // ============ 不喜欢列表 ============
    val dislikeRules: StateFlow<List<String>> = state(LxApp.app.dislikeRepository.rules, emptyList())

    fun addDislike(rule: String) {
        viewModelScope.launch { LxApp.app.dislikeRepository.add(rule) }
    }

    fun removeDislike(rule: String) {
        viewModelScope.launch { LxApp.app.dislikeRepository.remove(rule) }
    }

    // ============ 歌词 ============
    val showLyricTranslation: StateFlow<Boolean> = state(settings.showLyricTranslation, true)
    val showLyricRoma: StateFlow<Boolean> = state(settings.showLyricRoma, false)
    val lyricAlign: StateFlow<String> = state(settings.lyricAlign, "left")
    val lyricFontSize: StateFlow<Float> = state(settings.lyricFontSize, 1f)
    val isZoomActiveLrc: StateFlow<Boolean> = state(settings.isZoomActiveLrc, false)

    // ============ 搜索 ============
    val isShowHotSearch: StateFlow<Boolean> = state(settings.isShowHotSearch, true)
    val isShowHistorySearch: StateFlow<Boolean> = state(settings.isShowHistorySearch, true)

    // ============ 列表 ============
    val isShowSource: StateFlow<Boolean> = state(settings.isShowSource, true)
    val isShowAlbumName: StateFlow<Boolean> = state(settings.isShowAlbumName, false)
    val isShowInterval: StateFlow<Boolean> = state(settings.isShowInterval, true)
    val addMusicLocation: StateFlow<String> = state(settings.addMusicLocation, "bottom")

    // ============ 液态玻璃 ============
    val blurEnabled: StateFlow<Boolean> = state(settings.blurEnabled, true)
    val blurRadius: StateFlow<Float> = state(settings.blurRadius, 24f)

    // ============ 播放增强 ============
    val playbackRate: StateFlow<Float> = state(settings.playbackRate, 1f)
    val volume: StateFlow<Float> = state(settings.volume, 1f)
    val timeoutExitPlayed: StateFlow<Boolean> = state(settings.timeoutExitPlayed, true)

    // ============ 自定义音源 ============
    val userApiLog: StateFlow<Boolean> = state(settings.enableUserApiLog, false)

    fun cycleTheme() = launch { settings.setTheme(next(themeMode.value, listOf("system", "light", "dark", "monet"))) }

    fun cycleLang() = launch { settings.setLang(next(lang.value, listOf("system", "zh_cn", "zh_tw", "en"))) }

    fun setFontScale(value: Float) = launch { settings.setFontScale(value.coerceIn(0.8f, 1.6f)) }

    fun cycleShareType() = launch { settings.setShareType(next(shareType.value, listOf("system", "clipboard"))) }

    fun setAutoHidePlayBar(value: Boolean) = launch { settings.setAutoHidePlayBar(value) }

    fun cycleQuality() = launch { settings.setQuality(next(quality.value, listOf("128k", "192k", "320k", "flac", "flac24bit"))) }

    fun cyclePlayMode() = launch {
        val options = com.lingxu.music.core.player.PlayMode.ALL
        settings.setPlayMode(next(playMode.value, options))
    }

    fun setStartupAutoPlay(value: Boolean) = launch { settings.setStartupAutoPlay(value) }
    fun setIsSavePlayTime(value: Boolean) = launch { settings.setIsSavePlayTime(value) }
    fun setIsHandleAudioFocus(value: Boolean) = launch { settings.setIsHandleAudioFocus(value) }
    fun setPlaybackRate(value: Float) = launch { settings.setPlaybackRate(value.coerceIn(0.5f, 3f)) }
    fun setVolume(value: Float) = launch { settings.setVolume(value.coerceIn(0f, 1f)) }
    /**
     * 倍速/音量：滑块每帧都会回调，拖动中只更新播放器，落盘节流到 120ms 一次。
     * 松手时由 [commitPlaybackRate]/[commitVolume] 确保最终值落盘。
     */
    fun setPlaybackRateAndApply(value: Float) {
        val rate = value.coerceIn(0.5f, 3f)
        LxApp.app.playerRepository.setPlaybackRate(rate)
        if (throttle("rate", 120)) launch { settings.setPlaybackRate(rate) }
    }

    fun setVolumeAndApply(value: Float) {
        val vol = value.coerceIn(0f, 1f)
        LxApp.app.playerRepository.setVolume(vol)
        if (throttle("volume", 120)) launch { settings.setVolume(vol) }
    }

    fun commitPlaybackRate(value: Float) {
        val rate = value.coerceIn(0.5f, 3f)
        launch { settings.setPlaybackRate(rate) }
    }

    fun commitVolume(value: Float) {
        launch { settings.setVolume(value.coerceIn(0f, 1f)) }
    }

    fun setTimeoutExit(minutes: Int) = launch {
        settings.setTimeoutExitMinutes(minutes)
        LxApp.app.playerRepository.startTimeoutExit(minutes, settings.timeoutExitPlayed.first())
    }

    fun setTimeoutExitPlayed(value: Boolean) = launch {
        settings.setTimeoutExitPlayed(value)
        val current = settings.timeoutExitMinutes.first()
        if (current > 0) LxApp.app.playerRepository.startTimeoutExit(current, value)
    }

    fun setShowLyricTranslation(value: Boolean) = launch { settings.setShowLyricTranslation(value) }
    fun setShowLyricRoma(value: Boolean) = launch { settings.setShowLyricRoma(value) }
    fun cycleLyricAlign() = launch { settings.setLyricAlign(next(lyricAlign.value, listOf("left", "center"))) }
    fun setLyricFontSize(value: Float) = launch { settings.setLyricFontSize(value.coerceIn(0.8f, 1.8f)) }
    fun setIsZoomActiveLrc(value: Boolean) = launch { settings.setIsZoomActiveLrc(value) }

    fun setIsShowHotSearch(value: Boolean) = launch { settings.setIsShowHotSearch(value) }
    fun setIsShowHistorySearch(value: Boolean) = launch { settings.setIsShowHistorySearch(value) }

    fun setIsShowSource(value: Boolean) = launch { settings.setIsShowSource(value) }
    fun setIsShowAlbumName(value: Boolean) = launch { settings.setIsShowAlbumName(value) }
    fun setIsShowInterval(value: Boolean) = launch { settings.setIsShowInterval(value) }
    fun cycleAddMusicLocation() = launch { settings.setAddMusicLocation(next(addMusicLocation.value, listOf("bottom", "top"))) }

    fun setBlurEnabled(value: Boolean) = launch { settings.setBlurEnabled(value) }
    private var pendingBlurRadius: Float? = null

    /**
     * 模糊强度滑块每帧回调都会写 DataStore 并重建 GlassContext（整屏 backdrop 重画）。
     * 落盘节流到 200ms 一次：拖动时仍有视觉反馈，但不再每帧重画整屏。
     */
    fun setBlurRadius(value: Float) {
        pendingBlurRadius = value
        if (throttle("blur", 200)) launch { settings.setBlurRadius(value) }
    }

    /** 拖动结束时补一次落盘，确保最终值被保存 */
    fun commitBlurRadius() {
        val value = pendingBlurRadius ?: return
        pendingBlurRadius = null
        clearThrottle("blur")
        launch { settings.setBlurRadius(value) }
    }

    fun setUserApiLog(value: Boolean) = launch { settings.setEnableUserApiLog(value) }

    private fun next(current: String, options: List<String>): String =
        options[(options.indexOf(current) + 1) % options.size]

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
