package com.lxmusic.mobile.ui.screens.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.MusicInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val repo = LxApp.app.favoritesRepository
    private val musicRepo = LxApp.app.musicRepository
    private val playerRepo = LxApp.app.playerRepository

    val favorites: StateFlow<List<MusicInfo>> = repo.favorites

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(musicInfo: MusicInfo) {
        viewModelScope.launch {
            errorMessage = null
            runCatching {
                val url = musicRepo.getMusicUrl(musicInfo)
                val playInfo = musicInfo.copy(meta = musicInfo.meta.copy(filePath = url))
                playerRepo.playSingle(playInfo)
            }.onFailure { e ->
                errorMessage = "播放失败：${e.message}"
            }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { repo.remove(id) }
    }
}
