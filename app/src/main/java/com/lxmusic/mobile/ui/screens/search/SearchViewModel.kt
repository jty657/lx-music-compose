package com.lxmusic.mobile.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.MusicInfo
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    var results by mutableStateOf<List<MusicInfo>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun search(text: String, source: String) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            runCatching {
                LxApp.app.musicRepository.search(text, 1, source)
            }.onSuccess { list ->
                results = list
            }.onFailure { e ->
                errorMessage = e.message
                results = emptyList()
            }
            loading = false
        }
    }

    fun play(music: MusicInfo) {
        viewModelScope.launch {
            runCatching {
                val url = LxApp.app.musicRepository.getMusicUrl(music)
                val playInfo = music.copy(meta = music.meta.copy(filePath = url))
                LxApp.app.playerRepository.playSingle(playInfo)
            }.onFailure { e ->
                errorMessage = "获取播放链接失败：${e.message}"
            }
        }
    }
}
