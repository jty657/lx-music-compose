package com.lingxu.music.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    fun togglePlay() = LxApp.app.playerRepository.togglePlay()
    fun next() = LxApp.app.playerRepository.next()
    fun prev() = LxApp.app.playerRepository.prev()
    fun seekTo(positionMs: Long) = LxApp.app.playerRepository.seekTo(positionMs)
    fun setMode(mode: String) = LxApp.app.playerRepository.setPlayMode(mode)

    fun setPlaybackRate(rate: Float) {
        LxApp.app.playerRepository.setPlaybackRate(rate)
        viewModelScope.launch { LxApp.app.settings.setPlaybackRate(rate) }
    }

    fun setVolume(volume: Float) {
        LxApp.app.playerRepository.setVolume(volume)
        viewModelScope.launch { LxApp.app.settings.setVolume(volume) }
    }

    /**
     * 桌面歌词快捷开关（对齐原版播放页 MoreBtn 的 DesktopLyricBtn）。
     * 与 SettingViewModel.setDesktopLyricEnabled 同一语义：写偏好 + 有权限时立即建/拆悬浮窗。
     * 权限不足时由 UI 负责引导授权，这里只做「有权限才真正显示」的兜底，
     * 避免用户在系统设置撤销权限后再次打开开关时抛异常。
     */
    fun setDesktopLyricEnabled(value: Boolean) = viewModelScope.launch {
        LxApp.app.settings.setDesktopLyricEnabled(value)
        if (value) {
            val context = LxApp.app
            if (com.lingxu.music.core.system.DesktopLyric.canShow(context)) {
                com.lingxu.music.core.system.DesktopLyric.start(context)
            }
        } else {
            com.lingxu.music.core.system.DesktopLyric.stop()
        }
    }

    fun setTimeoutExit(minutes: Int) {
        viewModelScope.launch {
            LxApp.app.settings.setTimeoutExitMinutes(minutes)
            LxApp.app.playerRepository.startTimeoutExit(minutes, LxApp.app.settings.timeoutExitPlayed.first())
        }
    }

    fun formatTime(ms: Long): String {
        // 钳到非负：拖动进度到极小负值或异常状态时避免出现 "-1:-2" 之类的乱码时长
        val totalSec = (ms / 1000).coerceAtLeast(0L)
        val hour = totalSec / 3600
        val min = (totalSec % 3600) / 60
        val sec = totalSec % 60
        // 超过 1 小时的音频（播客/无损长曲/现场）用 H:MM:SS，避免 "75:30" 这类不易读的显示
        return if (hour > 0) "%d:%02d:%02d".format(hour, min, sec)
        else "%02d:%02d".format(min, sec)
    }
}
