package com.lingxu.music.ui.screens.mylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.db.Playlist
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.PlayingIndicator
import com.lingxu.music.ui.components.dialogOptionClickable
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.QualityBadge
import com.lingxu.music.ui.components.FilterChip
import com.lingxu.music.ui.components.TextActionButton
import com.lingxu.music.ui.components.rememberSavedLazyListState
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.LocalSaveScrollLocation
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
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
    val reduceMotion = LocalReduceMotion.current

    // 列表副标题字段开关：对齐原版 list.isShowSource / isShowInterval / isShowAlbumName。
    // 原版《我的列表》歌曲行（Mylist/MusicList/ListItem.tsx）会按 isShowAlbumName 追加专辑名、
    // 按 isShowInterval 显示时长；此前移植版该页副标题固定只有歌手名，三项开关在此页无效。
    // 组装方式与收藏页 FavoriteCard / 歌单详情一致，歌手恒显示，空片段过滤后无悬空「 · 」。
    val showSource by LxApp.app.settings.isShowSource.collectAsState(initial = true)
    val showInterval by LxApp.app.settings.isShowInterval.collectAsState(initial = true)
    val showAlbumName by LxApp.app.settings.isShowAlbumName.collectAsState(initial = false)
    // 当前播放曲目 id 与播放态：派生为 Pair 只订阅这两个字段，避免播放进度每秒变化触发整页重组。
    // 对齐原版《我的列表》activeIndex 高亮 —— 当前曲目用播放态音柱 + 主色歌名标识。
    val playingInfo: Pair<String?, Boolean> by LxApp.app.playerRepository.state
        .map { Pair(it.current?.id, it.isPlaying) }
        .collectAsState(initial = Pair(null, false))
    val currentPlayingId = playingInfo.first
    val isCurrentlyPlaying = playingInfo.second

    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var openList by remember { mutableStateOf<String?>(null) }
    // 长按歌曲弹出的操作菜单目标（null = 未打开）
    var actionTarget by remember { mutableStateOf<MusicInfo?>(null) }
    var musics by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }

    // 歌曲内搜索
    val searchState = remember { androidx.compose.foundation.text.input.TextFieldState() }
    val keyword = searchState.text.toString()
    // 多选模式
    var selectMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 多选方式：false=逐项点选（原版 list_select_single），true=范围选（原版 list_select_range）。
    // 范围选下点击一行会把「锚点行 → 当前行」之间的所有歌曲一次性选中，适合长列表批量操作；
    // 这是原版多选底栏的两种模式之一，移植版此前只有逐项点选。纯 UI 层状态，不碰播放/持久化逻辑。
    var selectRange by remember { mutableStateOf(false) }
    // 范围选锚点（shownMusics 中的下标）。只有范围选会用到；单选切回时清空，
    // 避免出现「模式已切回单选、锚点却残留」导致下次进入范围选的第一击选中一大段。
    var rangeAnchor by remember { mutableStateOf<Int?>(null) }
    // 批量移动到其它列表
    var moveTargets by remember { mutableStateOf(false) }
    // 目标列表对话框的模式：false=移动（加后从源移除），true=复制添加（只加不移除）。
    // 原版多选底栏同时提供「移动」与「添加」，移植版此前只有移动；复用同一对话框，仅切换落库行为。
    var targetModeAdd by remember { mutableStateOf(false) }
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
        rangeAnchor = null
        // 每次进入多选都从「单选」开始，对齐原版 MultipleModeBar 的初始 state('single')
        selectRange = false
    }

    /**
     * 多选点击：对齐原版 OnlineList/List.tsx 的 handleSelect。
     * - 单选：记录锚点（原版 prevSelectIndexRef），逐项增删；
     * - 范围选：已有锚点且已选中时，把 [锚点, 当前] 区间整体替换选中；点同一行清空；
     *   首次点击（无锚点/无选中）只选当前行并设为新锚点。
     * 用 shownMusics（搜索过滤后的可见列表）求下标，与用户看到的行序一致；
     * 搜索词变化导致锚点越界时按「无锚点」处理，不会因旧下标选错区间。
     */
    fun toggleSelect(music: MusicInfo) {
        val index = shownMusics.indexOfFirst { it.id == music.id }
        if (!selectRange) {
            // 单选：即使取消勾选也更新锚点（与原版 prevSelectIndexRef 一致），
            // 这样切到范围选后第一击就能从刚点过的位置开始圈选。
            rangeAnchor = index.takeIf { it >= 0 }
            selected = if (music.id in selected) selected - music.id else selected + music.id
            return
        }
        val anchor = rangeAnchor
        if (index >= 0 && anchor != null && anchor in shownMusics.indices && selected.isNotEmpty()) {
            if (anchor == index) {
                // 点回锚点：整体取消，与原版范围选「prevIndex == currentIndex -> newList = []」一致
                selected = emptySet()
            } else {
                val from = minOf(anchor, index)
                val to = maxOf(anchor, index)
                selected = shownMusics.subList(from, to + 1).map { it.id }.toSet()
            }
        } else {
            selected = setOf(music.id)
            rangeAnchor = index.takeIf { it >= 0 }
        }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.PageH, vertical = 10.dp),
            ) {
                BackIconButton(
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
                )
                Text(
                    openList?.let { id -> lists.firstOrNull { it.id == id }?.name } ?: "我的列表",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                    // 超长自定义列表名单行省略，避免换行把顶栏撑高、挤掉右侧「新建/导入/排序」按钮。
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                // 大屏宽度上限：内容不再被拉伸到 1000dp+，手机端完全无变化
                .widthIn(max = ContentMaxWidth)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))
        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        // 滚动位置记忆（原版 list.isSaveScrollLocation）：我的列表用固定 key
        val saveScroll = LocalSaveScrollLocation.current
        val listState = rememberSavedLazyListState(if (saveScroll) "myLists" else null)
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
                EmptyHint(
                    icon = MiuixIcons.Favorites,
                    text = if (musics.isEmpty()) "这个列表还没有歌曲" else "没有匹配的歌曲",
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
            ) {
                items(shownMusics, key = { it.id }) { music ->
                    val checked = music.id in selected
                    GlassSurface(
                        modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                        onClick = {
                            if (selectMode) {
                                toggleSelect(music)
                            } else {
                                viewModel.play(musics, music)
                            }
                        },
                        // 多选态下不弹菜单：否则长按与多选点击语义冲突
                        onLongClick = if (selectMode) null else ({ actionTarget = music }),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            val isCurrent = !selectMode && music.id == currentPlayingId
                            if (isCurrent) {
                                // 当前曲目：三条律动音柱（暂停时静帧），与播放队列页一致；
                                // 多选态下让位给选中/未选中图标，避免语义冲突。
                                PlayingIndicator(
                                    playing = isCurrentlyPlaying,
                                    modifier = Modifier.size(28.dp),
                                )
                            } else {
                                Icon(
                                    if (selectMode) {
                                        if (checked) MiuixIcons.Favorites else MiuixIcons.Play
                                    } else {
                                        MiuixIcons.Play
                                    },
                                    // 多选态下选中/未选中此前只靠颜色与图标区分，读屏用户无法感知
                                    contentDescription = if (selectMode) (if (checked) "已选中" else "未选中") else null,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        music.name,
                                        fontSize = MiuixTheme.textStyles.main.fontSize,
                                        color = if (isCurrent) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false),
                                    )
                                    // 音质角标（对齐原版）：紧跟歌名，歌名过长时优先压缩歌名
                                    QualityBadge(music, Modifier.padding(start = 6.dp))
                                }
                                // 副标题字段由设置开关决定，组装方式与收藏页 FavoriteCard 一致：
                                // 歌手恒显示，专辑名/来源/时长按开关按需拼接；空片段过滤后不会出现悬空的「 · 」。
                                val subtitle = buildList {
                                    add(music.singer)
                                    if (showAlbumName) music.meta.albumName.takeIf { it.isNotBlank() }?.let { add(it) }
                                    if (showSource) add(SourceConst.displayName(music.source))
                                    if (showInterval) music.interval?.let { add(it) }
                                }.filter { it.isNotBlank() }
                                Text(
                                    subtitle.joinToString(" · "),
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (!selectMode) {
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .deletePressClickable {
                                            scope.launch {
                                                viewModel.removeMusic(listId, music.id)
                                                musics = musics.filterNot { it.id == music.id }
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        MiuixIcons.Delete,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 多选操作栏：进入/退出多选时淡入并展开，避免底栏突然把列表顶短一截；
            // 窄屏 + 大字号下 4 个按钮会挤出屏幕，横向可滚动保证「移除」始终可达。
            // 减弱动效时淡入与展开都瞬时到位，避免高度变化干扰前庭敏感用户。
            AnimatedVisibility(
                visible = selectMode,
                enter = fadeIn(tween(if (reduceMotion) 0 else 160)) +
                    expandVertically(tween(if (reduceMotion) 0 else 200)),
                exit = fadeOut(tween(if (reduceMotion) 0 else 120)) +
                    shrinkVertically(tween(if (reduceMotion) 0 else 160)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                ) {
                    // 原版多选底栏提供「单选 / 范围选」两种模式（list_select_single / list_select_range），
                    // 移植版此前只有逐项点选。这里用全站公共 FilterChip 承载模式切换：
                    // 选中态主色淡底 + 按压缩放 + Role.Tab 读屏 + 减弱动效静帧，与搜索页/播放页 chip 同语言。
                    FilterChip(
                        label = "单选",
                        selected = !selectRange,
                        onClick = {
                            if (selectRange) {
                                selectRange = false
                                rangeAnchor = null
                            }
                        },
                        horizontalPadding = 10,
                    )
                    Spacer(Modifier.size(6.dp))
                    FilterChip(
                        label = "范围选",
                        selected = selectRange,
                        onClick = {
                            if (!selectRange) {
                                selectRange = true
                                rangeAnchor = null
                            }
                        },
                        horizontalPadding = 10,
                    )
                    Spacer(Modifier.size(8.dp))
                    // 批量操作按钮统一为 TextActionButton：48dp 热区 + 按压缩放 + Role.Button
                    // + 减弱动效静帧，与设置页/播放页/搜索页弹窗语言一致（原为裸 Button）。
                    TextActionButton(
                        text = if (selected.size == shownMusics.size) "取消全选" else "全选",
                        onClick = {
                            selected = if (selected.size == shownMusics.size) emptySet()
                            else shownMusics.map { it.id }.toSet()
                            // 全选/取消全选后锚点失去意义，清掉，避免下次范围选第一击误圈
                            rangeAnchor = null
                        },
                    )
                    Spacer(Modifier.size(8.dp))
                    TextActionButton(
                        text = "播放(${selected.size})",
                        onClick = {
                            val queue = shownMusics.filter { it.id in selected }
                            if (queue.isNotEmpty()) viewModel.play(queue, queue.first())
                        },
                    )
                    Spacer(Modifier.size(8.dp))
                    TextActionButton(
                        text = "添加",
                        onClick = { if (selected.isNotEmpty()) { targetModeAdd = true; moveTargets = true } },
                    )
                    Spacer(Modifier.size(8.dp))
                    TextActionButton(
                        text = "移动",
                        onClick = { if (selected.isNotEmpty()) { targetModeAdd = false; moveTargets = true } },
                    )
                    Spacer(Modifier.size(8.dp))
                    TextActionButton(
                        text = "移除",
                        onClick = {
                            scope.launch {
                                viewModel.removeMusics(listId, selected.toList())
                                musics = musics.filterNot { it.id in selected }
                                exitSelect()
                            }
                        },
                    )
                }
            }
        }
        }
    }

    // 长按弹出的歌曲操作菜单（播放走与点击同一条 viewModel.play，队列为本列表）
    actionTarget?.let { music ->
        com.lingxu.music.ui.components.SongActionMenu(
            music = music,
            onPlay = { viewModel.play(musics, music) },
            onDismiss = { actionTarget = null },
        )
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
                            .dialogOptionClickable {
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
                            // 纯文字选项热区补到约 48dp（可达性下限），字号与视觉不变
                            .padding(vertical = 14.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                }
                options.forEach { opt ->
                    Text(
                        opt.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .dialogOptionClickable {
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
                            // 排序选项热区补到约 48dp（可达性下限），字号与视觉不变
                            .padding(vertical = 14.dp),
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    "导出此歌单",
                    modifier = Modifier
                        .fillMaxWidth()
                        .dialogOptionClickable {
                            val target = lists.firstOrNull { it.id == openList }
                            showSort = false
                            // 列表名可以直接含路径分隔符，导出前净化（原版 utils.filterFileName）
                            if (target != null) exportLauncher.launch("${sanitizeFileName(target.name)}.json")
                        }
                        // 热区补到约 48dp（可达性下限），字号与视觉不变
                        .padding(vertical = 14.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "去除重复歌曲",
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .dialogOptionClickable {
                            // 同名同歌手视为重复，保留首次出现
                            val seen = mutableSetOf<String>()
                            val deduped = musics.filter { seen.add("${it.name}\u0000${it.singer}") }
                            if (deduped.size != musics.size) {
                                musics = deduped
                                scope.launch { viewModel.setMusics(openList!!, deduped) }
                            }
                            showSort = false
                        }
                        // 热区补到约 48dp（可达性下限），字号与视觉不变
                        .padding(vertical = 14.dp),
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
                Text(
                    if (targetModeAdd) "添加到" else "移动到",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                )
                Spacer(Modifier.size(12.dp))
                if (targetLists.isEmpty()) {
                    Text("没有其它列表")
                } else {
                    targetLists.forEach { target ->
                        Text(
                            target.name,
                            // 列表名用户可自定义、长度不可控：单行省略，避免超长名字撑高
                            // 选项行、把对话框顶出屏幕（与收藏页「加入列表」对话框一致）。
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .dialogOptionClickable {
                                    val moved = musics.filter { it.id in selected }
                                    scope.launch {
                                        if (targetModeAdd) {
                                            // 复制模式：只写入目标列表，源列表歌曲保留
                                            viewModel.addMusics(target.id, moved)
                                        } else {
                                            viewModel.moveMusics(openList!!, target.id, moved)
                                            // 仅移动模式从当前列表移除（添加模式源列表不动）
                                            musics = musics.filterNot { it.id in selected }
                                        }
                                        moveTargets = false
                                        exitSelect()
                                    }
                                }
                                // 目标列表项热区补到约 48dp（可达性下限），字号与视觉不变
                                .padding(vertical = 14.dp),
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
        // 与列表内空态、收藏页统一视觉语言：复用 EmptyHint（含入场淡入与减弱动效降级）
        EmptyHint(icon = MiuixIcons.Favorites, text = "还没有列表，点右上角「新建」或去歌单广场收藏")
        return
    }
    // 长按重命名此前无任何提示，用户无从发现（与音源页「长按音源可删除」同类）
    Text(
        "长按列表可重命名",
        fontSize = MiuixTheme.textStyles.footnote1.fontSize,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(start = 4.dp),
    )
    Spacer(Modifier.size(4.dp))
    LazyColumn(
        state = listState,
    ) {
        items(lists, key = { it.id }) { list ->
            GlassSurface(
                modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                onClick = { onOpen(list) },
                onLongClick = { onRename(list) },
                // 我的列表长按是「重命名列表」，非歌曲操作，读屏需如实播报。
                onLongClickLabel = "重命名列表",
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Icon(
                        if (list.sourceListId != null) MiuixIcons.Favorites else MiuixIcons.Play,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            list.name,
                            fontSize = MiuixTheme.textStyles.main.fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (list.sourceListId != null) "在线列表 · ${list.source}" else "本地列表",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        Modifier
                            .size(48.dp)
                            .deletePressClickable { onDelete(list) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            MiuixIcons.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(24.dp),
                        )
                    }
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

    /**
     * 把若干首歌复制添加到 toId（原版多选「添加」，不改动源列表）。
     * repository.addMusics 内部已按 id 去重，无需调用方再过滤。
     */
    fun addMusics(toId: String, musics: List<MusicInfo>) {
        viewModelScope.launch {
            runCatching { LxApp.app.myListsRepository.addMusics(toId, musics) }
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

/**
 * 删除/移除图标的点击修饰：48dp 热区由调用点的 size 提供，这里补按压缩放反馈。
 * 与收藏页删除按钮及全站 BackIconButton 同一手感：
 * 只动 transform（不触发重新布局），减弱动效时退化为静帧。
 */
@Composable
private fun Modifier.deletePressClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.85f else 1f,
        animationSpec = Motion.press(),
        label = "deletePress",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        // 与全站图标按钮一致补 Role.Button：
        // 内层 Icon 的 contentDescription（删除/移除）读屏会报成「图像」，加 role 后才是「按钮」。
        .clickable(
            indication = null,
            interactionSource = interactionSource,
            role = Role.Button,
            onClick = onClick,
        )
}
