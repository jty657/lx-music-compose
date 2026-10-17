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
        if (searchType == type) return
        searchType = type
        // 切换类型后旧结果不再适用，清空并重搜（否则会显示出另一类的旧列表）
        results = emptyList()
        songLists = emptyList()
        hasMore = false
        if (lastQuery.isNotBlank()) search(lastQuery, lastSource)
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
        if (!hasMore || loading) return
        val nextPage = page + 1
        viewModelScope.launch {
            loading = true
            errorMessage = null
            when (searchType) {
                SearchType.MUSIC -> runCatching {
                    LxApp.app.musicRepository.search(lastQuery, nextPage, lastSource)
                }.onSuccess { list ->
                    results = (results + list).distinctBy { it.id }
                    hasMore = list.isNotEmpty()
                    page = nextPage
                }.onFailure { e ->
                    // 保留 hasMore，让用户可以重试，而不是永久失去「加载更多」
                    errorMessage = "加载更多失败：${e.message}"
                }
                SearchType.SONGLIST -> runCatching {
                    LxApp.app.musicRepository.searchSongListAll(lastQuery, nextPage)
                }.onSuccess { pageResult ->
                    val merged = (songLists + pageResult.list).distinctBy { "${it.source}_${it.id}" }
                    songLists = merged
                    hasMore = pageResult.list.isNotEmpty()
                    page = nextPage
                }.onFailure { e ->
                    errorMessage = "加载更多失败：${e.message}"
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

    /** 播放搜索结果里的一首：整段结果作为队列，保留前后切歌能力（URL 由 playQueue 按需解析） */
    fun play(music: MusicInfo) {
        val index = results.indexOfFirst { it.id == music.id }
        if (index < 0) {
            LxApp.app.playerRepository.playSingle(music)
        } else {
            LxApp.app.playerRepository.playQueue(results, index)
        }
    }
}
