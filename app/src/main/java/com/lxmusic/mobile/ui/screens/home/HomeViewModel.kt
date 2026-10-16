package com.lxmusic.mobile.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.Board
import com.lxmusic.mobile.core.model.SourceConst
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var boards by mutableStateOf<List<Board>>(emptyList())
        private set

    var hotSearch by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedSource by mutableStateOf("kw")
        private set

    init {
        viewModelScope.launch {
            selectedSource = LxApp.app.settings.currentApiSource()
            loadBoards()
        }
    }

    fun sourceLabel(): String {
        if (selectedSource.startsWith("user_api")) {
            val info = LxApp.app.apiSourceRepository.let { repo ->
                // 从缓存列表查找名字
                null
            }
            return "自定义音源"
        }
        return when (selectedSource) {
            "kw" -> "酷我音乐"
            "kg" -> "酷狗音乐"
            "tx" -> "QQ音乐"
            "wy" -> "网易音乐"
            "mg" -> "咪咕音乐"
            else -> selectedSource
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
