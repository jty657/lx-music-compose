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

    fun setTimeoutExit(minutes: Int) {
        viewModelScope.launch {
            LxApp.app.settings.setTimeoutExitMinutes(minutes)
            LxApp.app.playerRepository.startTimeoutExit(minutes, LxApp.app.settings.timeoutExitPlayed.first())
        }
    }

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }
}
