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

    /** 已收藏的歌曲 id 集合 */
    var favoriteIds by mutableStateOf<Set<String>>(emptySet())
        private set

    /** 搜索历史（最近在前，最多 30 条） */
    var history by mutableStateOf<List<String>>(emptyList())
        private set

    private var lastQuery: String = ""
    private var lastSource: String = "all"
    private var page: Int = 1
    var hasMore by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            LxApp.app.favoritesRepository.favoriteIds.collect { favoriteIds = it }
        }
        viewModelScope.launch {
            history = LxApp.app.database.loadSearchHistory()
        }
    }

    fun search(text: String, source: String) {
        lastQuery = text
        lastSource = source
        page = 1
        viewModelScope.launch {
            loading = true
            errorMessage = null
            runCatching {
                LxApp.app.musicRepository.search(text, page, source)
            }.onSuccess { list ->
                results = list
                hasMore = list.isNotEmpty()
                recordHistory(text)
            }.onFailure { e ->
                errorMessage = e.message
                results = emptyList()
                hasMore = false
            }
            loading = false
        }
    }

    private suspend fun recordHistory(text: String) {
        val next = (listOf(text) + history.filterNot { it == text }).take(30)
        history = next
        LxApp.app.database.saveSearchHistory(next)
    }

    fun clearHistory() {
        viewModelScope.launch {
            history = emptyList()
            LxApp.app.database.saveSearchHistory(emptyList())
        }
    }

    /** 加载下一页 */
    fun loadMore() {
        if (loading || !hasMore || lastQuery.isBlank()) return
        val nextPage = page + 1
        viewModelScope.launch {
            loading = true
            runCatching {
                LxApp.app.musicRepository.search(lastQuery, nextPage, lastSource)
            }.onSuccess { list ->
                if (list.isEmpty()) {
                    hasMore = false
                } else {
                    // 按 id 去重追加
                    val existing = results.map { it.id }.toSet()
                    results = results + list.filterNot { it.id in existing }
                    page = nextPage
                }
            }
            loading = false
        }
    }

    fun toggleFavorite(music: MusicInfo) {
        viewModelScope.launch {
            LxApp.app.favoritesRepository.toggle(music)
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
