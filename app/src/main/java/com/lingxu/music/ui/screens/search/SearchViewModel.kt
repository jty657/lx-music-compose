package com.lingxu.music.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.launch

/** 搜索类型：音乐 / 歌单（对应原版 search.type） */
enum class SearchType { MUSIC, SONGLIST }

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

    /** 搜索类型：音乐 / 歌单（对应原版 search.type） */
    var searchType by mutableStateOf(SearchType.MUSIC)
        private set

    /** 搜索历史（最近在前，最多 30 条） */
    var history by mutableStateOf<List<String>>(emptyList())
        private set

    /** 歌单搜索结果 */
    var songLists by mutableStateOf<List<com.lingxu.music.core.model.SongListInfo>>(emptyList())
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
            when (searchType) {
                SearchType.MUSIC -> runCatching {
                    LxApp.app.musicRepository.search(text, page, source)
                }.onSuccess { list ->
                    results = list.distinctBy { it.id }
                    songLists = emptyList()
                    hasMore = list.isNotEmpty()
                    recordHistory(text)
                }.onFailure { e ->
                    errorMessage = e.message
                    results = emptyList()
                    hasMore = false
                }
                SearchType.SONGLIST -> runCatching {
                    LxApp.app.musicRepository.searchSongListAll(text, page)
                }.onSuccess { pageResult ->
                    songLists = pageResult.list.distinctBy { "${it.source}_${it.id}" }
                    results = emptyList()
                    hasMore = pageResult.list.isNotEmpty()
                    recordHistory(text)
                }.onFailure { e ->
                    errorMessage = e.message
                    songLists = emptyList()
                    hasMore = false
                }
            }
            loading = false
        }
    }

    fun setType(type: SearchType) {
        searchType = type
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

    /** 删除单条搜索历史 */
    fun removeHistory(word: String) {
        viewModelScope.launch {
            val next = history.filterNot { it == word }
            history = next
            LxApp.app.database.saveSearchHistory(next)
        }
    }

    /** 加载下一页 */
    fun loadMore() {
        if (!hasMore) return
        val nextPage = page + 1
        viewModelScope.launch {
            loading = true
            when (searchType) {
                SearchType.MUSIC -> runCatching {
                    LxApp.app.musicRepository.search(lastQuery, nextPage, lastSource)
                }.onSuccess { list ->
                    results = (results + list).distinctBy { it.id }
                    hasMore = list.isNotEmpty()
                    page = nextPage
                }.onFailure { hasMore = false }
                SearchType.SONGLIST -> runCatching {
                    LxApp.app.musicRepository.searchSongListAll(lastQuery, nextPage)
                }.onSuccess { pageResult ->
                    val merged = (songLists + pageResult.list).distinctBy { "${it.source}_${it.id}" }
                    songLists = merged
                    hasMore = pageResult.list.isNotEmpty()
                    page = nextPage
                }.onFailure { hasMore = false }
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
