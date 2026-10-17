package com.lingxu.music.ui.screens.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val repo = LxApp.app.favoritesRepository
    private val playerRepo = LxApp.app.playerRepository

    val favorites: StateFlow<List<MusicInfo>> = repo.favorites

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun play(musicInfo: MusicInfo) {
        // playQueue 会按需解析当前这首的 URL
        playerRepo.playSingle(musicInfo)
    }

    /** 播放全部（替换整个队列） */
    fun playAll(list: List<MusicInfo>) {
        if (list.isEmpty()) return
        playerRepo.playQueue(list, 0)
    }

    /** 加入「我的列表」（位置取设置项 list.addMusicLocationType） */
    fun addToList(listId: String, musicInfo: MusicInfo) {
        viewModelScope.launch {
            val location = LxApp.app.settings.addMusicLocation.first()
            runCatching { LxApp.app.myListsRepository.addMusics(listId, listOf(musicInfo), location) }
                .onFailure { errorMessage = "加入列表失败：${it.message}" }
        }
    }

    fun remove(id: String) {
        viewModelScope.launch { repo.remove(id) }
    }
}
