package com.lxmusic.mobile.ui.screens.favorite

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

    fun play(musicInfo: MusicInfo) {
        viewModelScope.launch {
            runCatching {
                val url = musicRepo.getMusicUrl(musicInfo)
                val playInfo = musicInfo.copy(meta = musicInfo.meta.copy(filePath = url))
                playerRepo.playSingle(playInfo)
            }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { repo.remove(id) }
    }
}
