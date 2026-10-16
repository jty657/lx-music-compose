package com.lxmusic.mobile.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.Board
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var boards by mutableStateOf<List<Board>>(emptyList())
        private set

    var hotSearch by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedSource by mutableStateOf("kw")
        private set

    private var sourceLabelText by mutableStateOf("酷我音乐")

    init {
        viewModelScope.launch {
            selectedSource = LxApp.app.settings.currentApiSource()
            refreshSourceLabel()
            loadBoards()
            loadHotSearch()
        }
    }

    /** 当前音源显示名（内置源用中文名，自定义源用脚本名） */
    fun sourceLabel(): String = sourceLabelText

    private suspend fun refreshSourceLabel() {
        val id = selectedSource
        sourceLabelText = if (id.startsWith("user_api")) {
            LxApp.app.apiSourceRepository.getAll().find { it.id == id }?.name
                ?: "自定义音源"
        } else {
            when (id) {
                "kw" -> "酷我音乐"
                "kg" -> "酷狗音乐"
                "tx" -> "QQ音乐"
                "wy" -> "网易音乐"
                "mg" -> "咪咕音乐"
                else -> id
            }
        }
    }

    fun loadBoards() {
        viewModelScope.launch {
            // 榜单用当前内置源（用户音源不提供榜单）
            val source = if (selectedSource.startsWith("user_api")) "kw" else selectedSource
            runCatching {
                LxApp.app.musicRepository.getBoardsList(source)
            }.onSuccess { list ->
                boards = list
            }.onFailure {
                boards = emptyList()
            }
        }
    }

    /** 热搜词（失败时为空，首页会回退展示榜单） */
    fun loadHotSearch() {
        viewModelScope.launch {
            val source = if (selectedSource.startsWith("user_api")) "kw" else selectedSource
            runCatching {
                LxApp.app.musicRepository.getHotSearch(source)
            }.onSuccess { list ->
                hotSearch = list
            }.onFailure {
                hotSearch = emptyList()
            }
        }
    }

    fun loadBoard(board: Board) {
        viewModelScope.launch {
            val source = board.id.substringBefore("__")
            runCatching {
                val detail = LxApp.app.musicRepository.getBoardList(source, board.bangid, 1)
                if (detail.list.isNotEmpty()) {
                    LxApp.app.playerRepository.playQueue(detail.list, 0)
                }
            }
        }
    }
}
