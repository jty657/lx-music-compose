package com.lingxu.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SourceConst
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
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val textFieldState = remember { TextFieldState() }
    val sources = listOf("all") + SourceConst.ONLINE
    var selectedSource by rememberSaveable { mutableStateOf("all") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        // 搜索输入框
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(Modifier.height(8.dp))

        // 音源选择
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            sources.forEach { source ->
                Button(
                    onClick = { selectedSource = source },
                ) {
                    Text(
                        text = SourceConst.displayName(source),
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
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

        // 错误提示
        viewModel.errorMessage?.let { err ->
            Text(
                err,
                color = MiuixTheme.colorScheme.error,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        // 搜索历史（无结果且输入为空时展示；可在设置里关闭）
        val showHistory by LxApp.app.settings.isShowHistorySearch.collectAsState(initial = true)
        if (showHistory && viewModel.results.isEmpty() && viewModel.songLists.isEmpty() && viewModel.history.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("搜索历史", fontSize = MiuixTheme.textStyles.body2.fontSize)
                Spacer(Modifier.weight(1f))
                Text(
                    "清空",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.clearHistory() },
                )
            }
            Spacer(Modifier.height(4.dp))
            viewModel.history.forEach { word ->
                Text(
                    word,
                    fontSize = MiuixTheme.textStyles.body1.fontSize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            textFieldState.setTextAndPlaceCursorAtEnd(word)
                            scope.launch { viewModel.search(word, selectedSource) }
                        }
                        .padding(vertical = 6.dp),
                )
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        // 结果列表
        val results = viewModel.results
        val songLists = viewModel.songLists
        if (songLists.isNotEmpty()) {
            LazyColumn(Modifier.weight(1f)) {
                items(songLists, key = { "${it.source}_${it.id}" }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
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
            }
            return@Column
        }

        val resultState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(resultState)
        LazyColumn(state = resultState) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                Text(
                    "${music.singer} · ${music.interval ?: ""}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
            Icon(
                if (isFavorite) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onToggleFavorite,
                    ),
            )
        }
    }
}
