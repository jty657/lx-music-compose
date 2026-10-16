package com.lingxu.music.ui.screens.songlist

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
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SongListTag
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
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

    fun load(next: Int) {
        if (loading) return
        loading = true
        error = null
        viewModel.load(source, sorts.getOrNull(sortIndex)?.second ?: "", selectedTag, next) { result ->
            list = if (next == 1) result else list + result.filterNot { it.id in list.map { x -> x.id } }
            hasMore = result.isNotEmpty()
            page = next
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

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        GlassTopBar(modifier = Modifier.padding(horizontal = -12.dp, vertical = -12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                top.yukonga.miuix.kmp.basic.Icon(
                    MiuixIcons.Back,
                    contentDescription = "返回",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = { navigator.pop() },
                        ),
                )
                Text(
                    com.lingxu.music.core.model.SourceConst.displayName(source) + " 歌单",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
        Spacer(Modifier.size(8.dp))

        // 排序
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sorts) { (name, _) ->
                val idx = sorts.indexOfFirst { it.first == name }
                Button(onClick = { sortIndex = idx }) {
                    Text(
                        name,
                        color = if (sortIndex == idx) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(Modifier.size(6.dp))

        // 标签
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    Button(onClick = { selectedTag = null }) {
                        Text("全部", color = if (selectedTag == null) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface)
                    }
                }
                items(tags) { tag ->
                    Button(onClick = { selectedTag = tag.id }) {
                        Text(
                            tag.name,
                            color = if (selectedTag == tag.id) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
        }

        error?.let {
            Text(it, color = MiuixTheme.colorScheme.error, fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.size(4.dp))
        }

        if (list.isEmpty() && !loading) {
            Text(
                "暂无歌单",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            items(list, key = { "${it.source}_${it.id}" }) { item ->
                GlassSurface(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    onClick = {
                        navigator.push(Screen.SongListDetail(item.source, item.id, item.name,
))
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
                    Button(
                        onClick = { load(page + 1) },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    ) { Text(if (loading) "加载中…" else "加载更多") }
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
        onResult: (List<SongListInfo>) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getSongList(source, sortId, tag ?: "", page).list
            }.onSuccess(onResult).onFailure { onResult(emptyList()) }
        }
    }

    /** 长按歌单 = 收藏到「我的列表」（对应原版 collectList） */
    fun collect(item: SongListInfo) {
        viewModelScope.launch {
            runCatching { LxApp.app.myListsRepository.collectSongList(item) }
        }
    }
}
