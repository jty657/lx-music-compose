package com.lingxu.music.ui.screens.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val repo = LxApp.app.favoritesRepository
    private val playerRepo = LxApp.app.playerRepository

    val favorites: StateFlow<List<MusicInfo>> = repo.favorites

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(musicInfo: MusicInfo) {
        viewModelScope.launch {
            errorMessage = null
            runCatching {
                val resolved = playerRepo.resolvePlayInfo(musicInfo)
                playerRepo.playSingle(resolved)
            }.onFailure { e ->
                errorMessage = "播放失败：${e.message}"
            }
        }
    }

    /** 播放全部（替换整个队列） */
    fun playAll(list: List<MusicInfo>) {
        if (list.isEmpty()) return
        viewModelScope.launch {
            errorMessage = null
            runCatching {
                val resolved = list.map { playerRepo.resolvePlayInfo(it) }
                playerRepo.playQueue(resolved, 0)
            }.onFailure { e ->
                errorMessage = "播放失败：${e.message}"
            }
        }
    }

    /** 加入「我的列表」（对应原版 addMusicLocationType） */
    fun addToList(listId: String, musicInfo: MusicInfo) {
        viewModelScope.launch {
            runCatching { LxApp.app.myListsRepository.addMusics(listId, listOf(musicInfo)) }
                .onFailure { errorMessage = "加入列表失败：${it.message}" }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { repo.remove(id) }
    }
}
