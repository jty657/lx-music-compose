package com.lingxu.music.ui.screens.player

import androidx.lifecycle.ViewModel
import com.lingxu.music.LxApp

class PlayerViewModel : ViewModel() {

    fun togglePlay() = LxApp.app.playerRepository.togglePlay()
    fun next() = LxApp.app.playerRepository.next()
    fun prev() = LxApp.app.playerRepository.prev()
    fun seekTo(positionMs: Long) = LxApp.app.playerRepository.seekTo(positionMs)
    fun setMode(mode: String) = LxApp.app.playerRepository.setPlayMode(mode)

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }
}
