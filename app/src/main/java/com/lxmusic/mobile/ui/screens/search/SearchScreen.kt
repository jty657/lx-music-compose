package com.lxmusic.mobile.ui.screens.search

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.MusicInfo
import com.lxmusic.mobile.core.model.SourceConst
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import com.lxmusic.mobile.ui.navigation.Screen
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
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val textFieldState = remember { TextFieldState() }
    val sources = listOf("all") + SourceConst.ONLINE
    var selectedSource by remember { mutableStateOf("all") }

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

        // 结果列表
        val results = viewModel.results
        LazyColumn {
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
