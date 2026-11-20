package com.lingxu.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun SearchScreen(initialKeyword: String = "", viewModel: SearchViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val textFieldState = remember { TextFieldState() }
    val sources = listOf("all") + SourceConst.ONLINE
    var selectedSource by rememberSaveable { mutableStateOf("all") }

    // 由热词/历史进入时自动搜索
    androidx.compose.runtime.LaunchedEffect(initialKeyword) {
        if (initialKeyword.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(initialKeyword)
            viewModel.search(initialKeyword, selectedSource)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // 搜索输入框（磨砂玻璃条）
        GlassTopBar {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                TextField(
                    state = textFieldState,
                    label = "搜索歌曲、歌手",
                    useLabelAsPlaceholder = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = {
                        val text = textFieldState.text.toString()
                        if (text.isNotBlank()) {
                            scope.launch {
                                viewModel.search(text, selectedSource)
                            }
                        }
                    },
                ) {
                    Icon(MiuixIcons.Search, contentDescription = "搜索")
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.height(8.dp))

        // 音源选择（横向滚动：6 个按钮挤在固定 Row 里会溢出屏幕）
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(sources) { source ->
                val selected = selectedSource == source
                GlassSurface(
                    tint = if (selected) MiuixTheme.colorScheme.primary.copy(alpha = 0.16f) else null,
                    onClick = { selectedSource = source },
                ) {
                    Text(
                        text = SourceConst.displayName(source),
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = if (selected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 搜索类型切换（对应原版 音乐/歌单）
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { viewModel.setType(SearchType.MUSIC) }) {
                Text(
                    "音乐",
                    color = if (viewModel.searchType == SearchType.MUSIC) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurface,
                )
            }
            Button(onClick = { viewModel.setType(SearchType.SONGLIST) }) {
                Text(
                    "歌单",
                    color = if (viewModel.searchType == SearchType.SONGLIST) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 错误提示统一放在结果列表底部的可点「重试项」展示（见下方两个分支），
        // 这里不再单独渲染红字，避免加载失败时同一句错误文案出现两遍。

        // 搜索历史（无结果且输入为空时展示；可在设置里关闭）
        val showHistory by LxApp.app.settings.isShowHistorySearch.collectAsState(initial = true)
        // 必须加「输入框为空」判断：搜索后输入框仍保留关键词，若只判结果为空，
        // 任意无结果的搜索都会命中历史分支并 return，导致「没有找到相关音乐」空态永远不显示。
        val inputIsBlank = textFieldState.text.toString().isBlank()
        if (showHistory && inputIsBlank && viewModel.results.isEmpty() && viewModel.songLists.isEmpty() && viewModel.history.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("搜索历史", fontSize = MiuixTheme.textStyles.body2.fontSize)
                Spacer(Modifier.weight(1f))
                Text(
                    "长按删除单条",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "清空",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.primary,
                    // 扩大点击热区：仅靠文字高度约 20dp，低于 48dp 的可访问性下限，
                    // 上下各补 10dp 让手指更容易点中（历史行整体仍保持紧凑）
                    modifier = Modifier
                        .clickable { viewModel.clearHistory() }
                        .padding(vertical = 10.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            // 历史最多 30 条，小屏上平铺会超出屏幕且下方条目不可见；用 weight + 滚动承载
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                viewModel.history.forEach { word ->
                    Text(
                        word,
                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    textFieldState.setTextAndPlaceCursorAtEnd(word)
                                    scope.launch { viewModel.search(word, selectedSource) }
                                },
                                onLongClick = { viewModel.removeHistory(word) },
                            )
                            .padding(vertical = 6.dp),
                    )
                }
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        // 结果列表
        val results = viewModel.results
        val songLists = viewModel.songLists
        if (songLists.isNotEmpty()) {
            val songListState = rememberLazyListState()
            // 与音乐结果分支一致：歌单结果滚动也要上报，迷你播放条才会随滚动隐藏
            com.lingxu.music.ui.components.ReportScrollForMiniBar(songListState)
            // 滚到接近底部时自动加载下一页（留 4 项余量，用户不必伸手点按钮）
            AutoLoadMore(songListState, viewModel.hasMore, viewModel.loading) { viewModel.loadMore() }
            LazyColumn(state = songListState, modifier = Modifier.weight(1f)) {
                items(songLists, key = { "${it.source}_${it.id}" }) { item ->
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            navigator.push(Screen.SongListDetail(item.source, item.id, item.name))
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Icon(MiuixIcons.Favorites, contentDescription = null, modifier = Modifier.size(28.dp))
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(item.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                                Text(
                                    "${item.author} · ${SourceConst.displayName(item.source)}",
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
                if (viewModel.hasMore) {
                    item(key = "__load_more__") {
                        Button(
                            onClick = { viewModel.loadMore() },
                            enabled = !viewModel.loading,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        ) { Text(if (viewModel.loading) "加载中…" else "加载更多") }
                    }
                }
                // 自动加载失败时给一个可点的重试入口，否则用户会一直卡在列表底部
                viewModel.errorMessage?.let { err ->
                    item(key = "__load_error__") {
                        RetryHint(text = err, onClick = { viewModel.loadMore() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            return@Column
        } else if (viewModel.searchType == SearchType.SONGLIST &&
            !viewModel.loading && viewModel.hasSearched && viewModel.errorMessage == null
        ) {
            // 歌单搜索无结果：此前会 fall through 到音乐分支误显「没有找到相关音乐」，
            // 这里给出对应歌单的空态后直接返回，避免误导。
            Column(
                Modifier.fillMaxWidth().padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    MiuixIcons.Favorites,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "没有找到相关歌单",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            return@Column
        }

        val resultState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(resultState)
        AutoLoadMore(resultState, viewModel.hasMore, viewModel.loading) { viewModel.loadMore() }
        // 搜索后无结果：给出明确空态，避免只剩一片空白让用户以为卡住
        if (results.isEmpty() && !viewModel.loading && viewModel.hasSearched && viewModel.errorMessage == null) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    MiuixIcons.Search,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "没有找到相关音乐",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        LazyColumn(state = resultState, modifier = Modifier.weight(1f)) {
            items(results, key = { it.id }) { music ->
                SearchResultCard(
                    music = music,
                    isFavorite = music.id in viewModel.favoriteIds,
                    onClick = {
                        scope.launch {
                            viewModel.play(music)
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(music) },
                )
            }
            if (viewModel.hasMore) {
                item(key = "__load_more__") {
                    Button(
                        onClick = { viewModel.loadMore() },
                        enabled = !viewModel.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(if (viewModel.loading) "加载中…" else "加载更多")
                    }
                }
            }
            viewModel.errorMessage?.let { err ->
                item(key = "__load_error__") {
                    RetryHint(text = err, onClick = { viewModel.loadMore() }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        }
    }
}

@Composable
private fun SearchResultCard(
    music: MusicInfo,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(32.dp))
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
                    "${music.singer} · ${music.interval ?: ""}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // 可用音质标签（对应原版列表项的音质角标）
                val qualities = music.qualitys.mapNotNull { it.type }.distinct()
                if (qualities.isNotEmpty()) {
                    Text(
                        qualities.joinToString(" "),
                        fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onToggleFavorite,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isFavorite) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/** 滚到接近底部（留 4 项余量）时自动触发加载下一页 */
@Composable
private fun AutoLoadMore(
    listState: androidx.compose.foundation.lazy.LazyListState,
    hasMore: Boolean,
    loading: Boolean,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState, hasMore, loading) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            val last = visible.lastOrNull()?.index ?: 0
            last to listState.layoutInfo.totalItemsCount
        }.collect { (last, total) ->
            if (hasMore && !loading && total > 0 && last >= total - 4) {
                onLoadMore()
            }
        }
    }
}
