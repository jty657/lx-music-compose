package com.lingxu.music.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.flow.first
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

    /**
     * 持久化的搜索音源（对应原版 search.source）。null = 冷启动尚未读到。
     * 与 searchType 同属原版 getSearchSetting/saveSearchSetting：切源即落盘、冷启动恢复。
     * 移植版此前 selectedSource 仅 rememberSaveable，进程死亡后回到 "all"，重启丢失用户选择。
     */
    var persistedSource by mutableStateOf<String?>(null)
        private set

    /** 搜索历史（最近在前，最多 30 条） */
    var history by mutableStateOf<List<String>>(emptyList())
        private set

    /** 热搜词（对齐原版搜索页空态热搜；失败时为空，界面回退到其它空态） */
    var hotWords by mutableStateOf<List<String>>(emptyList())
        private set

    /** 歌单搜索结果 */
    var songLists by mutableStateOf<List<com.lingxu.music.core.model.SongListInfo>>(emptyList())
        private set

    private var lastQuery: String = ""
    private var lastSource: String = "all"
    private var page: Int = 1
    /**
     * 搜索请求序号：每次发起新搜索/换源/切换类型都递增，只有最新请求的结果允许写入状态。
     * 快速连点搜索或切换音源时，旧请求可能后于新请求返回；没有守卫会用过期结果覆盖当前列表，
     * 表现为「搜了 B 却显示 A 的结果」。以序号作轻量版本锁，避免为每次请求引入协程取消的复杂度。
     */
    private var requestSeq: Int = 0
    var hasMore by mutableStateOf(false)
        private set

    /** 是否已发起过搜索：用于区分「尚未搜索」与「搜索后无结果」，避免首屏误显空态 */
    var hasSearched by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            LxApp.app.favoritesRepository.favoriteIds.collect { favoriteIds = it }
        }
        viewModelScope.launch {
            history = LxApp.app.database.loadSearchHistory()
        }
        // 对齐原版 getSearchSetting：冷启动恢复上次的搜索类型（音乐/歌单）
        viewModelScope.launch {
            val saved = LxApp.app.settings.searchType.first()
            searchType = if (saved == "songlist") SearchType.SONGLIST else SearchType.MUSIC
        }
        // 对齐原版 getSearchSetting 的 source 字段：冷启动恢复上次搜索音源
        viewModelScope.launch {
            persistedSource = LxApp.app.settings.searchSource.first()
        }
    }

    /**
     * 按音源拉取热搜（对齐原版 BlankView 的切源刷新热搜行为）。
     * 热搜词来自内置 SDK，用户音源不提供：all（聚合）与 user_api 回落到 kw，与原版默认一致。
     * 首次进入由 SearchScreen 的 selectedSource effect 触发，切源时再次触发，失败静默为空由界面回退。
     */
    fun refreshHot(source: String) {
        val s = if (source == "all" || source.startsWith("user_api")) "kw" else source
        viewModelScope.launch {
            runCatching { LxApp.app.musicRepository.getHotSearch(s) }
                .onSuccess { hotWords = it.take(12) }
        }
    }

    /** 搜索联想词（对齐原版搜索框输入下拉；空列表时界面不展示） */
    var suggestions by mutableStateOf<List<String>>(emptyList())
        private set

    private var tipJob: kotlinx.coroutines.Job? = null
    private var tipSeq: Int = 0
    private var tipSource: String = "kw"

    /**
     * 输入变化时拉取联想词（防抖 250ms，对齐原版 debounceTipSearch 200ms）。
     * 文案不变或为空时直接清空；切源时由 [setTipSource] 触发重取，保证下拉与当前音源一致。
     */
    fun suggest(text: String, source: String, immediate: Boolean = false) {
        tipSource = if (source == "all") tipSource else source
        tipJob?.cancel()
        // 代次号在取消之后立即递增：被取消的旧协程即使已越过挂起点也无法回写
        val seq = ++tipSeq
        if (text.isBlank()) {
            suggestions = emptyList()
            return
        }
        tipJob = viewModelScope.launch {
            if (!immediate) kotlinx.coroutines.delay(250)
            val list = LxApp.app.musicRepository.getSearchTips(text, tipSource)
            if (seq == tipSeq) suggestions = list
        }
    }

    /** 音源切换后按新音源重新拉取联想 */
    fun setTipSource(source: String) {
        if (source != "all") tipSource = source
    }

    /** 联想词被点击/搜索确认后收起下拉 */
    fun clearSuggestions() {
        tipJob?.cancel()
        suggestions = emptyList()
    }

    fun search(text: String, source: String) {
        val seq = ++requestSeq
        lastQuery = text
        lastSource = source
        page = 1
        hasSearched = true
        // 搜索确认后收起联想下拉（对齐原版：按下搜索键立即隐藏 TipList）
        clearSuggestions()
        // 同步置位：让翻页守卫能立即感知「正在搜索」，避免 launch 异步执行前 loadMore 插队重复请求
        loading = true
        errorMessage = null
        viewModelScope.launch {
            when (searchType) {
                SearchType.MUSIC -> runCatching {
                    LxApp.app.musicRepository.search(text, page, source)
                }.onSuccess { list ->
                    if (seq != requestSeq) return@onSuccess
                    results = list.distinctBy { it.id }
                    songLists = emptyList()
                    hasMore = list.isNotEmpty()
                    recordHistory(text)
                }.onFailure { e ->
                    if (seq != requestSeq) return@onFailure
                    errorMessage = e.message
                    results = emptyList()
                    hasMore = false
                }
                SearchType.SONGLIST -> runCatching {
                    LxApp.app.musicRepository.searchSongListAll(text, page)
                }.onSuccess { pageResult ->
                    if (seq != requestSeq) return@onSuccess
                    songLists = pageResult.list.distinctBy { "${it.source}_${it.id}" }
                    results = emptyList()
                    hasMore = pageResult.list.isNotEmpty()
                    recordHistory(text)
                }.onFailure { e ->
                    if (seq != requestSeq) return@onFailure
                    errorMessage = e.message
                    songLists = emptyList()
                    hasMore = false
                }
            }
            // 只有最新请求有权结束 loading；过期请求不能把新请求的进行中状态清掉
            if (seq == requestSeq) loading = false
        }
    }

    fun setType(type: SearchType) {
        if (searchType == type) return
        searchType = type
        // 对齐原版 saveSearchSetting：类型选择即时落盘，冷启动恢复
        viewModelScope.launch {
            LxApp.app.settings.setSearchType(if (type == SearchType.SONGLIST) "songlist" else "music")
        }
        // 切换类型后旧结果不再适用，清空并重搜（否则会显示出另一类的旧列表）
        results = emptyList()
        songLists = emptyList()
        hasMore = false
        if (lastQuery.isNotBlank()) search(lastQuery, lastSource)
    }

    /** 对齐原版 saveSearchSetting({source})：切源即时落盘，冷启动恢复（白名单由调用方校验） */
    fun saveSource(source: String) {
        viewModelScope.launch {
            LxApp.app.settings.setSearchSource(source)
        }
    }

    private suspend fun recordHistory(text: String) {
        // 对齐原版 core/search/search.ts addHistoryWord：未开启「显示搜索历史」时不记录，
        // 避免用户在设置里关掉后仍被悄悄保存。
        if (!LxApp.app.settings.isShowHistorySearch.first()) return
        // 原版历史上限 15 条（store/search/action.ts：长度 >= 15 时截断到 14 再 unshift）
        val next = (listOf(text) + history.filterNot { it == text }).take(15)
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
        // 捕获当前批次序号：翻页属于当前这次搜索，期间若发起新搜索则本页结果作废，
        // 否则「加载更多」的迟到响应会把上一批的旧结果追加到新搜索的列表里。
        val seq = requestSeq
        val nextPage = page + 1
        // 同步置位，封住两次快速触发 loadMore 之间的竞态窗口（原来 loading 在 launch 内才置 true）
        loading = true
        errorMessage = null
        viewModelScope.launch {
            when (searchType) {
                SearchType.MUSIC -> runCatching {
                    LxApp.app.musicRepository.search(lastQuery, nextPage, lastSource)
                }.onSuccess { list ->
                    if (seq != requestSeq) return@onSuccess
                    results = (results + list).distinctBy { it.id }
                    hasMore = list.isNotEmpty()
                    page = nextPage
                }.onFailure { e ->
                    if (seq != requestSeq) return@onFailure
                    // 保留 hasMore，让用户可以重试，而不是永久失去「加载更多」
                    errorMessage = "加载更多失败：${e.message}"
                }
                SearchType.SONGLIST -> runCatching {
                    LxApp.app.musicRepository.searchSongListAll(lastQuery, nextPage)
                }.onSuccess { pageResult ->
                    if (seq != requestSeq) return@onSuccess
                    val merged = (songLists + pageResult.list).distinctBy { "${it.source}_${it.id}" }
                    songLists = merged
                    hasMore = pageResult.list.isNotEmpty()
                    page = nextPage
                }.onFailure { e ->
                    if (seq != requestSeq) return@onFailure
                    errorMessage = "加载更多失败：${e.message}"
                }
            }
            if (seq == requestSeq) loading = false
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
