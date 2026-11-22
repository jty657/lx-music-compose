package com.lingxu.music.ui.screens.songlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.appendDistinctById
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SongListTag
import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.FilterChip
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.LoadMoreButton
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
import com.lingxu.music.ui.navigation.Screen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 歌单广场：按平台展示歌单列表（对应原版 screens/Home/Views/Songlist）
 * 支持排序切换、标签筛选、搜索、收藏到「我的列表」
 */
@Composable
fun SongListScreen(
    source: String,
    viewModel: SongListViewModel = viewModel(),
) {
    val navigator = LocalNavigator.current
    var list by remember { mutableStateOf<List<SongListInfo>>(emptyList()) }
    var tags by remember { mutableStateOf<List<SongListTag>>(emptyList()) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var sorts by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var sortIndex by remember { mutableStateOf(0) }
    var page by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    // 请求代次：切标签/排序时旧请求的结果会被丢弃，且不再用 loading 早退（那样会吞掉新请求）
    var requestId by remember { mutableStateOf(0) }

    fun load(next: Int) {
        val reqId = ++requestId
        loading = true
        error = null
        viewModel.load(source, sorts.getOrNull(sortIndex)?.second ?: "", selectedTag, next) { result, message ->
            if (reqId != requestId) return@load
            error = message
            list = if (next == 1) result else list.appendDistinctById(result) { it.id }
            hasMore = result.isNotEmpty()
            if (message == null) page = next
            loading = false
        }
    }

    LaunchedEffect(source, sortIndex, selectedTag) {
        page = 1
        hasMore = true
        load(1)
    }

    LaunchedEffect(source) {
        sorts = viewModel.sorts(source)
        viewModel.loadTags(source) { info ->
            tags = (info.hotTag + info.tags.flatMap { it.list }).distinctBy { it.id }
        }
    }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                BackIconButton(onClick = { navigator.pop() })
                Text(
                    com.lingxu.music.core.model.SourceConst.displayName(source) + " 歌单",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                    modifier = Modifier.padding(start = 12.dp),
                )
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

        // 排序：与搜索页音源/类型 chip 同一套「选中 = 主色淡底 + 描边 + 读屏 selected」语言。
        // 此前是裸 Button 只靠文字颜色区分选中态，圆角形态与筛选语义都不一致。
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sorts) { (name, _) ->
                val idx = sorts.indexOfFirst { it.first == name }
                FilterChip(
                    label = name,
                    selected = sortIndex == idx,
                    onClick = { sortIndex = idx },
                )
            }
        }

        Spacer(Modifier.size(6.dp))

        // 标签
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        label = "全部",
                        selected = selectedTag == null,
                        onClick = { selectedTag = null },
                    )
                }
                items(tags) { tag ->
                    FilterChip(
                        label = tag.name,
                        selected = selectedTag == tag.id,
                        onClick = { selectedTag = tag.id },
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
        }

        // 错误提示统一由下方空态分支的 RetryHint 展示，此处不再单独渲染红字，
        // 避免加载失败且列表为空时同一句错误文案出现两遍。

        if (list.isEmpty() && !loading) {
            if (error != null) {
                RetryHint(text = "加载失败，点击重试", onClick = { load(1) })
            } else {
                // 与全站空态统一：居中 + 占位图标 + 留白，替代原左对齐的一行小字
                EmptyHint(icon = MiuixIcons.Favorites, text = "暂无歌单")
            }
            return@Column
        }
        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            items(list, key = { "${it.source}_${it.id}" }) { item ->
                GlassSurface(
                    modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                    onClick = {
                        navigator.push(Screen.SongListDetail(item.source, item.id, item.name))
                    },
                    onLongClick = { viewModel.collect(item) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            MiuixIcons.Favorites,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                        )
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(item.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                            Text(
                                "${item.author} · ${item.playCount ?: ""}",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
            if (hasMore) {
                item(key = "__songlist_more__") {
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

class SongListViewModel : ViewModel() {
    fun sorts(source: String) = LxApp.app.musicRepository.getSongListSorts(source)

    fun loadTags(source: String, onResult: (com.lingxu.music.core.model.SongListTagInfo) -> Unit) {
        viewModelScope.launch {
            runCatching { LxApp.app.musicRepository.getSongListTags(source) }
                .onSuccess(onResult)
                .onFailure { onResult(com.lingxu.music.core.model.SongListTagInfo(source = source)) }
        }
    }

    fun load(
        source: String,
        sortId: String,
        tag: String?,
        page: Int,
        onResult: (List<SongListInfo>, String?) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getSongList(source, sortId, tag ?: "", page).list
            }.onSuccess { onResult(it, null) }
                .onFailure { onResult(emptyList(), it.message ?: "加载失败") }
        }
    }

    /**
     * 长按歌单 = 收藏到「我的列表」（对应原版 collectList）。
     * 收藏后立刻拉一次内容：否则收藏完打开是空列表，还要用户自己点「同步到最新」。
     */
    fun collect(item: SongListInfo) {
        viewModelScope.launch {
            runCatching {
                val list = LxApp.app.myListsRepository.collectSongList(item)
                LxApp.app.myListsRepository.syncOnlineList(list)
            }
        }
    }
}
