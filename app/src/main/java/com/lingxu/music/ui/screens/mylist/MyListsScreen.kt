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
import androidx.compose.foundation.lazy.rememberLazyListState
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
    // 列表内排序 / 去重
    var showSort by remember { mutableStateOf(false) }
    // 在线列表同步（进度弹窗）
    var showSync by remember { mutableStateOf(false) }
    // 列表内歌曲加载的代次（丢弃过期回调）
    var listLoadToken by remember { mutableStateOf(0) }
    var syncCount by remember { mutableStateOf(0) }
    var toast by remember { mutableStateOf<String?>(null) }
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    // 单列表导出
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val target = lists.firstOrNull { it.id == openList } ?: return@rememberLauncherForActivityResult
        scope.launch {
            com.lingxu.music.core.repository.ListTransfer.export(appContext, uri, target.name, musics)
                .onSuccess { toast = "已导出 $it 首" }
                .onFailure { toast = "导出失败：${it.message}" }
        }
    }
    // 单列表导入
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            com.lingxu.music.core.repository.ListTransfer.read(appContext, uri)
                .onSuccess { data -> viewModel.importList(data.name, data.musics) { toast = it } }
                .onFailure { toast = "导入失败：${it.message}" }
        }
    }

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

    // 系统返回键：优先退出多选 / 退出列表内层，与顶部返回箭头行为一致
    androidx.activity.compose.BackHandler(enabled = selectMode || openList != null) {
        when {
            selectMode -> exitSelect()
            openList != null -> {
                openList = null
                musics = emptyList()
                searchState.setTextAndPlaceCursorAtEnd("")
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
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
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) { Text("导入") }
                } else {
                    Button(onClick = { showSort = true }) { Text("排序") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { if (selectMode) exitSelect() else selectMode = true }) {
                        Text(if (selectMode) "完成" else "多选")
                    }
                }
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))
        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)

        if (openList == null) {
            ListOfLists(
                lists = lists,
                listState = listState,
                onOpen = { list ->
                    openList = list.id
                    searchState.setTextAndPlaceCursorAtEnd("")
                    // 代次：快速返回再进另一个列表时，先发的响应会被丢弃，避免显示上一个列表的歌
                    val token = ++listLoadToken
                    viewModel.loadMusics(list.id) { loaded ->
                        if (token == listLoadToken) musics = loaded
                    }
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

            LazyColumn(
                state = listState,
            ) {
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

    if (showSort) {
        data class SortOption(val label: String, val key: String, val desc: Boolean)
        val options = listOf(
            SortOption("歌曲名 升序", "name", false),
            SortOption("歌曲名 降序", "name", true),
            SortOption("歌手 升序", "singer", false),
            SortOption("歌手 降序", "singer", true),
            SortOption("专辑 升序", "album", false),
            SortOption("时长 升序", "interval", false),
            SortOption("时长 降序", "interval", true),
            SortOption("随机打乱", "random", false),
        )
        GlassDialog(onDismissRequest = { showSort = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("列表操作", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                val currentList = lists.firstOrNull { it.id == openList }
                if (currentList?.sourceListId != null) {
                    Text(
                        "同步到最新",
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val target = currentList
                                showSort = false
                                showSync = true
                                viewModel.syncList(
                                    target,
                                    onProgress = { syncCount = it },
                                    onDone = {
                                        showSync = false
                                        syncCount = 0
                                        toast = it
                                        viewModel.loadMusics(target.id) { musics = it }
                                    },
                                )
                            }
                            .padding(vertical = 10.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                }
                options.forEach { opt ->
                    Text(
                        opt.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val sorted = when (opt.key) {
                                    "name" -> musics.sortedBy { it.name }
                                    "singer" -> musics.sortedBy { it.singer }
                                    "album" -> musics.sortedBy { it.meta.albumName }
                                    "interval" -> musics.sortedBy { parseIntervalSeconds(it.interval) }
                                    else -> musics.shuffled()
                                }.let { if (opt.desc) it.reversed() else it }
                                musics = sorted
                                scope.launch { viewModel.setMusics(openList!!, sorted) }
                                showSort = false
                            }
                            .padding(vertical = 10.dp),
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    "导出此歌单",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val target = lists.firstOrNull { it.id == openList }
                            showSort = false
                            // 列表名可以直接含路径分隔符，导出前净化（原版 utils.filterFileName）
                            if (target != null) exportLauncher.launch("${sanitizeFileName(target.name)}.json")
                        }
                        .padding(vertical = 10.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "去除重复歌曲",
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 同名同歌手视为重复，保留首次出现
                            val seen = mutableSetOf<String>()
                            val deduped = musics.filter { seen.add("${it.name}\u0000${it.singer}") }
                            if (deduped.size != musics.size) {
                                musics = deduped
                                scope.launch { viewModel.setMusics(openList!!, deduped) }
                            }
                            showSort = false
                        }
                        .padding(vertical = 10.dp),
                )
            }
        }
    }

    if (showSync) {
        GlassDialog(onDismissRequest = {
            // 弱网下逐页拉取可能很久，必须能让用户取消（否则连返回键都被 Dialog 吃掉）
            viewModel.cancelSync()
            showSync = false
            syncCount = 0
            toast = "已取消同步"
        }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("正在同步", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text("已拉取 $syncCount 首…")
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        viewModel.cancelSync()
                        showSync = false
                        syncCount = 0
                        toast = "已取消同步"
                    }) { Text("取消同步") }
                }
            }
        }
    }

    if (toast != null) {
        GlassDialog(onDismissRequest = { toast = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(toast!!, fontSize = MiuixTheme.textStyles.body1.fontSize)
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { toast = null }) { Text("好") }
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
    listState: androidx.compose.foundation.lazy.LazyListState,
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
    LazyColumn(
        state = listState,
    ) {
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

/** 去掉文件名里非法/危险的字符（对应原版 utils.filterFileName） */
private fun sanitizeFileName(name: String): String =
    name.replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001f]"), "_")
        .trim()
        .takeIf { it.isNotBlank() } ?: "歌单"

class MyListsViewModel : ViewModel() {
    val lists = LxApp.app.myListsRepository.lists

    fun loadMusics(listId: String, onResult: (List<MusicInfo>) -> Unit) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.getMusics(listId) }.onSuccess(onResult) }
    }

    /** 取消正在进行的在线列表同步（弱网下逐页拉取可能很久） */
    fun cancelSync() {
        syncJob?.cancel()
        syncJob = null
    }

    private var syncJob: kotlinx.coroutines.Job? = null

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

    /** 覆盖写入整个列表（排序/去重后使用） */
    fun setMusics(listId: String, musics: List<MusicInfo>) {
        viewModelScope.launch { runCatching { LxApp.app.myListsRepository.saveMusics(listId, musics) } }
    }

    /** 导入单个歌单（新建一个本地列表并写入歌曲） */
    fun importList(name: String, musics: List<MusicInfo>, onDone: (String) -> Unit) {
        viewModelScope.launch {
            // 按 id 过滤空条目与重复项（导入的重复 id 会让列表页 LazyColumn 因重复 key 崩溃）
            val cleaned = com.lingxu.music.core.repository.ListTransfer.filterMusicList(musics)
            runCatching {
                val list = LxApp.app.myListsRepository.create(name.ifBlank { "导入的歌单" })
                LxApp.app.myListsRepository.addMusics(list.id, cleaned)
            }.onSuccess { onDone("已导入 ${cleaned.size} 首") }
                .onFailure { e ->
                    // 序列化异常对使用者毫无意义，翻译成人话
                    val msg = if (e is IllegalArgumentException) {
                        "导入失败：文件不是有效的歌单数据"
                    } else {
                        "导入失败：${e.message}"
                    }
                    onDone(msg)
                }
        }
    }

    /**
     * 同步在线列表（对应原版 core/syncSourceList.ts）：逐页拉全远端后覆盖本地。
     * @param onProgress 已拉取数量
     * @param onDone 完成回调（结果描述）
     */
    fun syncList(list: Playlist, onProgress: (Int) -> Unit, onDone: (String) -> Unit) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            val source = list.source
            val sourceListId = list.sourceListId
            if (source == null || sourceListId == null) {
                onDone("该列表不是在线列表")
                return@launch
            }
            try {
                val collected = LxApp.app.myListsRepository.pullOnlineMusics(
                    source = source,
                    sourceListId = sourceListId,
                    onProgress = { onProgress(it) },
                )
                LxApp.app.myListsRepository.overwriteOnlineMusics(list.id, collected)
                onDone("同步完成（${collected.size} 首）")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone("同步失败：${e.message}")
            }
        }
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
                // URL 由 playQueue 按需解析
                LxApp.app.playerRepository.playQueue(queue, index)
            }
        }
    }
}

/** "mm:ss" / "hh:mm:ss" → 秒；解析失败按 0（用于按时长排序，字符串比较会把 10:00 排在 2:00 前面） */
private fun parseIntervalSeconds(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    val parts = value.split(":").mapNotNull { it.trim().toLongOrNull() }
    if (parts.isEmpty()) return 0L
    var seconds = 0L
    for (p in parts) seconds = seconds * 60 + p
    return seconds
}
