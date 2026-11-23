package com.lingxu.music.ui.screens.songlist

import androidx.compose.foundation.clickable
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
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.LoadMoreButton
import com.lingxu.music.ui.components.LoadingHint
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.components.TopBarTitle
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
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

/** 歌单详情页：歌单内歌曲列表，支持播放与收藏到我的列表 */
@Composable
fun SongListDetailScreen(
    source: String,
    id: String,
    name: String,
    viewModel: SongListDetailViewModel = viewModel(),
) {
    val navigator = LocalNavigator.current
    var musicList by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }
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
        // 不要命名为 id：会遮蔽本页的 id（歌单标识）参数
        val reqId = ++requestId
        loading = true
        error = null
        viewModel.load(source, id, next) { list, message ->
            if (reqId != requestId) return@load
            error = message
            musicList = if (next == 1) list else musicList.appendDistinctById(list) { it.id }
            hasMore = list.isNotEmpty()
            if (message == null) page = next
            loading = false
        }
    }

    LaunchedEffect(source, id) { load(1) }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Dimens.PageH, vertical = 10.dp)) {
            BackIconButton(onClick = { navigator.pop() })
            // 单行省略 + weight：歌单名可能很长（「2024 华语年度热歌 TOP500」），
            // 此前无 maxLines，长标题会让顶栏行高翻倍并挤掉右侧空间
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

        Row {
            // 列表尚未加载完成时禁用「播放全部」，避免空列表点击无任何反馈
            Button(
                onClick = { if (musicList.isNotEmpty()) viewModel.playAll(musicList) },
                enabled = musicList.isNotEmpty(),
            ) {
                Text("播放全部")
            }
            Spacer(Modifier.size(8.dp))
            Button(onClick = { viewModel.collect(source, id, name) }) {
                Text("收藏歌单")
            }
        }

        Spacer(Modifier.size(8.dp))

        if (musicList.isNotEmpty()) {
            TextField(
                state = searchState,
                label = "在此歌单内搜索",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
        }

        error?.let {
            RetryHint(text = "$it（点击重试）", onClick = { load(1) })
        }
        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            // 首次加载中且暂无内容：给出明确的进度提示，避免用户分不清是加载中还是无数据
            if (loading && musicList.isEmpty() && error == null) {
                item(key = "__songlist_loading__") {
                    // 统一加载态：三点流水灯呼吸替代此前的静态死文本，慢网络上明确表达「进行中」，
                    // 与首页骨架、加载更多 Crossfade 全站语言一致；减弱动效时静帧。
                    LoadingHint(text = "正在加载歌单…")
                }
            }
            // 加载完成、无错误、且（含筛选后）无歌曲：显示空态
            if (!loading && error == null && shownMusic.isEmpty()) {
                item(key = "__songlist_empty__") {
                    EmptyHint(
                        icon = MiuixIcons.Favorites,
                        text = if (keyword.isBlank()) "这个歌单暂时没有歌曲" else "没有匹配的歌曲",
                    )
                }
            }
            items(shownMusic, key = { it.id }) { music ->
                GlassSurface(
                    modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                    onClick = { viewModel.play(musicList, music) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        // 整卡已可点且读屏会播报歌名/歌手，这里只是装饰图标；描述置空避免重复播报「播放」
                        Icon(MiuixIcons.Play, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.size(10.dp))
                        TrackCover(url = music.meta.picUrl, size = Dimens.CoverRow, corner = Dimens.CornerS)
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
                item(key = "__songlist_detail_more__") {
                    LoadMoreButton(
                        loading = loading,
                        onClick = { load(page + 1) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
        }
        }
    }
}

class SongListDetailViewModel : ViewModel() {
    fun load(source: String, id: String, page: Int, onResult: (List<MusicInfo>, String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getSongListDetail(source, id, page).list
            }.onSuccess { onResult(it, null) }
                .onFailure { onResult(emptyList(), it.message ?: "加载失败") }
        }
    }

    fun play(queue: List<MusicInfo>, music: MusicInfo) {
        viewModelScope.launch {
            runCatching {
                val index = queue.indexOfFirst { it.id == music.id }.coerceAtLeast(0)
                // URL 由 playQueue 按需解析（只解析当前这首再开播），不要在这里解析整列表
                LxApp.app.playerRepository.playQueue(queue, index)
            }
        }
    }

    fun playAll(queue: List<MusicInfo>) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.playerRepository.playQueue(queue, 0)
            }
        }
    }

    fun collect(source: String, id: String, name: String) {
        viewModelScope.launch {
            runCatching {
                val list = LxApp.app.myListsRepository.collectSongList(
                    com.lingxu.music.core.model.SongListInfo(id = id, name = name, source = source),
                )
                LxApp.app.myListsRepository.syncOnlineList(list)
            }
        }
    }
}
