package com.lingxu.music.ui.screens.songlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.navigation.LocalNavigator
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
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
    val shownMusic = if (keyword.isBlank()) musicList
    else musicList.filter {
        it.name.contains(keyword, ignoreCase = true) || it.singer.contains(keyword, ignoreCase = true)
    }

    fun load(next: Int) {
        if (loading) return
        loading = true
        error = null
        viewModel.load(source, id, next) { list ->
            musicList = if (next == 1) list else musicList + list.filterNot { it.id in musicList.map { m -> m.id } }
            hasMore = list.isNotEmpty()
            page = next
            loading = false
        }
    }

    LaunchedEffect(source, id) { load(1) }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Icon(
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
                name,
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))

        Row {
            Button(onClick = { if (musicList.isNotEmpty()) viewModel.playAll(musicList) }) {
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
            Text(it, color = MiuixTheme.colorScheme.error, fontSize = MiuixTheme.textStyles.body2.fontSize)
        }

        LazyColumn(Modifier.weight(1f)) {
            items(shownMusic, key = { it.id }) { music ->
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { viewModel.play(musicList, music) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                            Text(
                                "${music.singer} · ${SourceConst.displayName(music.source)}",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
            if (hasMore) {
                item(key = "__songlist_detail_more__") {
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
}

class SongListDetailViewModel : ViewModel() {
    fun load(source: String, id: String, page: Int, onResult: (List<MusicInfo>) -> Unit) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getSongListDetail(source, id, page).list
            }.onSuccess(onResult).onFailure { onResult(emptyList()) }
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

    fun playAll(queue: List<MusicInfo>) {
        viewModelScope.launch {
            runCatching {
                val resolved = queue.map { info -> LxApp.app.playerRepository.resolvePlayInfo(info) }
                LxApp.app.playerRepository.playQueue(resolved, 0)
            }
        }
    }

    fun collect(source: String, id: String, name: String) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.myListsRepository.collectSongList(
                    com.lingxu.music.core.model.SongListInfo(id = id, name = name, source = source),
                )
            }
        }
    }
}
