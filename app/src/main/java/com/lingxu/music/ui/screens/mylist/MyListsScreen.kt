package com.lingxu.music.ui.screens.mylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.db.Playlist
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.navigation.LocalNavigator
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 我的列表：本地歌单管理（新建/重命名/删除）与歌单内歌曲（搜索/多选批量操作） */
@Composable
fun MyListsScreen(viewModel: MyListsViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val lists by viewModel.lists.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var openList by remember { mutableStateOf<String?>(null) }
    var musics by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }

    // 歌曲内搜索
    val searchState = remember { androidx.compose.foundation.text.input.TextFieldState() }
    val keyword = searchState.text.toString()
    // 多选模式
    var selectMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 批量移动到其它列表
    var moveTargets by remember { mutableStateOf(false) }

    val shownMusics = remember(musics, keyword) {
        if (keyword.isBlank()) musics
        else musics.filter {
            it.name.contains(keyword, ignoreCase = true) || it.singer.contains(keyword, ignoreCase = true)
        }
    }

    fun exitSelect() {
        selectMode = false
        selected = emptySet()
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        GlassTopBar(modifier = Modifier.padding(horizontal = -12.dp, vertical = -12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    MiuixIcons.Back,
                    contentDescription = "返回",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = {
                                when {
                                    selectMode -> exitSelect()
                                    openList != null -> {
                                        openList = null
                                        musics = emptyList()
                                        searchState.setTextAndPlaceCursorAtEnd("")
                                    }
                                    else -> navigator.pop()
                                }
                            },
                        ),
                )
                Text(
                    openList?.let { id -> lists.firstOrNull { it.id == id }?.name } ?: "我的列表",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
                if (openList == null) {
                    Button(onClick = { showCreate = true }) { Text("新建") }
                } else {
                    Button(onClick = { if (selectMode) exitSelect() else selectMode = true }) {
                        Text(if (selectMode) "完成" else "多选")
                    }
                }
            }
        }
        Spacer(Modifier.size(8.dp))

        if (openList == null) {
            ListOfLists(
                lists = lists,
                onOpen = { list ->
                    openList = list.id
                    searchState.setTextAndPlaceCursorAtEnd("")
                    viewModel.loadMusics(list.id) { musics = it }
                },
                onRename = { renaming = it },
                onDelete = { scope.launch { viewModel.remove(it.id) } },
            )
        } else {
            val listId = openList!!
            // 搜索框（列表内搜索）
            if (musics.isNotEmpty()) {
                TextField(
                    state = searchState,
                    label = "在此列表内搜索",
                    useLabelAsPlaceholder = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
            }

            if (shownMusics.isEmpty()) {
                Text(
                    if (musics.isEmpty()) "这个列表还没有歌曲" else "没有匹配的歌曲",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }

            LazyColumn(Modifier.weight(1f)) {
                items(shownMusics, key = { it.id }) { music ->
                    val checked = music.id in selected
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            if (selectMode) {
                                selected = if (checked) selected - music.id else selected + music.id
                            } else {
                                viewModel.play(musics, music)
                            }
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Icon(
                                if (selectMode) {
                                    if (checked) MiuixIcons.Favorites else MiuixIcons.Play
                                } else {
                                    MiuixIcons.Play
                                },
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                                Text(
                                    music.singer,
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            if (!selectMode) {
                                Icon(
                                    MiuixIcons.Delete,
                                    contentDescription = "移除",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            onClick = {
                                                scope.launch {
                                                    viewModel.removeMusic(listId, music.id)
                                                    musics = musics.filterNot { it.id == music.id }
                                                }
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            // 多选操作栏
            if (selectMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Button(onClick = {
                        selected = if (selected.size == shownMusics.size) emptySet()
                        else shownMusics.map { it.id }.toSet()
                    }) { Text(if (selected.size == shownMusics.size) "取消全选" else "全选") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        val queue = shownMusics.filter { it.id in selected }
                        if (queue.isNotEmpty()) viewModel.play(queue, queue.first())
                    }) { Text("播放(${selected.size})") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { if (selected.isNotEmpty()) moveTargets = true }) { Text("移动") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        scope.launch {
                            viewModel.removeMusics(listId, selected.toList())
                            musics = musics.filterNot { it.id in selected }
                            exitSelect()
                        }
                    }) { Text("移除") }
                }
            }
        }
    }

    if (showCreate) {
        GlassDialog(onDismissRequest = { showCreate = false; newName = "" }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("新建列表", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("列表名称") },
                    singleLine = true,
                )
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { showCreate = false; newName = "" }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        if (newName.isNotBlank()) viewModel.create(newName.trim())
                        newName = ""
                        showCreate = false
                    }) { Text("创建") }
                }
            }
        }
    }

    renaming?.let { list ->
        var name by remember(list.id) { mutableStateOf(list.name) }
        GlassDialog(onDismissRequest = { renaming = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("重命名", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                )
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { renaming = null }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        if (name.isNotBlank()) viewModel.rename(list.id, name.trim())
                        renaming = null
                    }) { Text("保存") }
                }
            }
        }
    }

    if (moveTargets) {
        val targetLists = lists.filter { it.id != openList }
        GlassDialog(onDismissRequest = { moveTargets = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("移动到", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                if (targetLists.isEmpty()) {
                    Text("没有其它列表")
                } else {
                    targetLists.forEach { target ->
                        Text(
                            target.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val moved = musics.filter { it.id in selected }
                                    scope.launch {
                                        viewModel.moveMusics(openList!!, target.id, moved)
                                        musics = musics.filterNot { it.id in selected }
                                        moveTargets = false
                                        exitSelect()
                                    }
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

/** 列表清单（未进入某个列表时） */
@Composable
private fun androidx.compose.foundation.layout.ColumnScope.ListOfLists(
    lists: List<Playlist>,
    onOpen: (Playlist) -> Unit,
    onRename: (Playlist) -> Unit,
    onDelete: (Playlist) -> Unit,
) {
    if (lists.isEmpty()) {
        Text(
            "还没有列表，点右上角「新建」或去歌单广场收藏",
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
        )
        return
    }
    LazyColumn(Modifier.weight(1f)) {
        items(lists, key = { it.id }) { list ->
            GlassSurface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { onOpen(list) },
                onLongClick = { onRename(list) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Icon(
                        if (list.sourceListId != null) MiuixIcons.Favorites else MiuixIcons.Play,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(list.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                        Text(
                            if (list.sourceListId != null) "在线列表 · ${list.source}" else "本地列表",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Icon(
                        MiuixIcons.Delete,
                        contentDescription = "删除",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = { onDelete(list) },
                            ),
                    )
                }
            }
        }
    }
}

class MyListsViewModel : ViewModel() {
    val lists = LxApp.app.myListsRepository.lists

    fun loadMusics(listId: String, onResult: (List<MusicInfo>) -> Unit) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.getMusics(listId) }.onSuccess(onResult) }
    }

    fun create(name: String) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.create(name) } }
    }

    fun rename(id: String, name: String) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.rename(id, name) } }
    }

    fun remove(id: String) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.remove(id) } }
    }

    fun removeMusic(listId: String, musicId: String) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.removeMusics(listId, listOf(musicId)) } }
    }

    fun removeMusics(listId: String, musicIds: List<String>) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.removeMusics(listId, musicIds) } }
    }

    /** 把若干首歌从 fromId 移到 toId */
    fun moveMusics(fromId: String, toId: String, musics: List<MusicInfo>) {
        viewModelScope.launch {
            runCatching {
                val repo = LxApp.app.myListsRepository
                repo.addMusics(toId, musics)
                repo.removeMusics(fromId, musics.map { it.id })
            }
        }
    }

    fun play(queue: List<MusicInfo>, music: MusicInfo) {
        viewModelScope.launch {
            runCatching {
                val index = queue.indexOfFirst { it.id == music.id }.coerceAtLeast(0)
                val resolved = queue.map { info -> LxApp.app.playerRepository.resolvePlayInfo(info) }
                LxApp.app.playerRepository.playQueue(resolved, index)
            }
        }
    }
}
