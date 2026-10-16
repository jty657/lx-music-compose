package com.lingxu.music.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.Board
import com.lingxu.music.core.model.SourceConst
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
            SourceConst.displayName(id)
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

    /** 点榜单：返回（源, bangId, 名称）供导航到详情页 */
    fun boardTarget(board: Board): Triple<String, String, String> {
        val source = board.id.substringBefore("__")
        return Triple(source, board.bangid, board.name)
    }
}
