package com.lingxu.music.ui.screens.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.appendDistinctById
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.LoadMoreButton
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.components.TopBarTitle
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.Motion
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 榜单详情页：展示某榜单的歌曲列表，支持播放与加载更多 */
@Composable
fun BoardDetailScreen(
    source: String,
    bangId: String,
    name: String,
    viewModel: BoardDetailViewModel = viewModel(),
) {
    val navigator = LocalNavigator.current
    var musicList by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    val searchState = remember { androidx.compose.foundation.text.input.TextFieldState() }
    val keyword = searchState.text.toString()
    val shownMusic = remember(musicList, keyword) {
        if (keyword.isBlank()) musicList
        else musicList.filter {
            it.name.contains(keyword, ignoreCase = true) || it.singer.contains(keyword, ignoreCase = true)
        }
    }

    // 请求代次：旧请求的结果会被丢弃，且不再用 loading 早退（那样会吞掉新请求）
    var requestId by remember { mutableStateOf(0) }

    fun load(next: Int) {
        val reqId = ++requestId
        loading = true
        error = null
        viewModel.load(source, bangId, next) { list, message ->
            if (reqId != requestId) return@load
            error = message
            musicList = if (next == 1) list else musicList.appendDistinctById(list) { it.id }
            hasMore = list.isNotEmpty()
            if (message == null) page = next
            loading = false
        }
    }

    LaunchedEffect(source, bangId) {
        load(1)
    }

    Column(
        Modifier
            .fillMaxSize(),
    ) {
        GlassTopBar {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            BackIconButton(onClick = { navigator.pop() })
            // 单行省略 + weight：长榜单名不再把下面的操作按钮挤出版面
            TopBarTitle(name)
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

        if (musicList.isEmpty() && !loading) {
            if (error != null) {
                RetryHint(text = "加载失败，点击重试", onClick = { load(1) })
            } else {
                // 与全站空态统一：居中 + 占位图标 + 留白，替代原左对齐的一行小字
                EmptyHint(icon = MiuixIcons.Favorites, text = "暂无歌曲")
            }
            return@Column
        }

        if (musicList.isNotEmpty()) {
            Row {
                Button(onClick = { viewModel.playAll(musicList) }) {
                    Text("播放全部")
                }
                Spacer(Modifier.size(8.dp))
                Button(onClick = {
                    viewModel.collectBoard(source, bangId, name) { toast = it }
                }) {
                    Text("收藏榜单")
                }
            }
            Spacer(Modifier.size(8.dp))
            TextField(
                state = searchState,
                label = "在此榜单内搜索",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
        }
        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            // 首次加载中且暂无内容：给出明确的进度提示，避免分不清「加载中」还是「无数据」
            if (loading && musicList.isEmpty() && error == null) {
                item(key = "__board_loading__") {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "正在加载榜单…",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            // 搜索关键词无匹配：给出专属空态，避免过滤后一片空白
            if (!loading && error == null && musicList.isNotEmpty() && shownMusic.isEmpty()) {
                item(key = "__board_search_empty__") {
                    EmptyHint(icon = MiuixIcons.Favorites, text = "没有匹配的歌曲")
                }
            }
            items(shownMusic, key = { it.id }) { music ->
                GlassSurface(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = Motion.itemFade(),
                            placementSpec = Motion.itemPlacement(),
                            fadeOutSpec = Motion.itemFade(),
                        )
                        .fillMaxWidth()
                        .padding(vertical = Dimens.ItemGap),
                    onClick = { viewModel.play(musicList, music) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(28.dp))
                        Spacer(Modifier.size(10.dp))
                        TrackCover(url = music.meta.picUrl, size = 44.dp, corner = 8.dp)
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                music.name,
                                fontSize = MiuixTheme.textStyles.main.fontSize,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${music.singer} · ${SourceConst.displayName(music.source)}",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            if (hasMore) {
                item(key = "__board_more__") {
                    LoadMoreButton(
                        loading = loading,
                        onClick = { load(page + 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
        }
    }

    toast?.let { msg ->
        com.lingxu.music.ui.components.GlassDialog(onDismissRequest = { toast = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(msg, fontSize = MiuixTheme.textStyles.body1.fontSize)
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { toast = null }) { Text("好") }
                }
            }
        }
    }
}

class BoardDetailViewModel : ViewModel() {

    fun load(source: String, bangId: String, page: Int, onResult: (List<MusicInfo>, String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getBoardList(source, bangId, page).list
            }.onSuccess { onResult(it, null) }
                .onFailure { onResult(emptyList(), it.message ?: "加载失败") }
        }
    }

    fun play(queue: List<MusicInfo>, music: MusicInfo) {
        viewModelScope.launch {
            runCatching {
                val index = queue.indexOfFirst { it.id == music.id }.coerceAtLeast(0)
                // URL 由 playQueue 按需解析（只解析当前这首再开播）
                LxApp.app.playerRepository.playQueue(queue, index)
            }
        }
    }

    fun playAll(queue: List<MusicInfo>) {
        if (queue.isEmpty()) return
        play(queue, queue.first())
    }

    /** 收藏榜单为「我的列表」（对应原版 Leaderboard 的收藏） */
    fun collectBoard(source: String, bangId: String, name: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { LxApp.app.myListsRepository.collectBoard(source, bangId, name) }
                .onSuccess { onDone("已收藏到我的列表") }
                .onFailure { onDone("收藏失败：${it.message}") }
        }
    }
}
