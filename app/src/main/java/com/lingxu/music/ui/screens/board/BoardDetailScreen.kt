package com.lingxu.music.ui.screens.board

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
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
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

    fun load(next: Int) {
        if (loading) return
        loading = true
        viewModel.load(source, bangId, next) { list ->
            musicList = if (next == 1) list else musicList + list.filterNot { it.id in musicList.map { m -> m.id } }
            hasMore = list.isNotEmpty()
            page = next
            loading = false
        }
    }

    LaunchedEffect(source, bangId) {
        load(1)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        GlassTopBar(modifier = Modifier.padding(horizontal = -12.dp, vertical = -12.dp)) {
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
        Spacer(Modifier.size(8.dp))

        if (musicList.isEmpty() && !loading) {
            Text(
                "暂无歌曲",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            items(musicList, key = { it.id }) { music ->
                GlassSurface(
    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
    onClick = { viewModel.play(musicList, music,
) },
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
                item(key = "__board_more__") {
                    Button(
                        onClick = { load(page + 1) },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Text(if (loading) "加载中…" else "加载更多")
                    }
                }
            }
        }
    }
}

class BoardDetailViewModel : ViewModel() {

    fun load(source: String, bangId: String, page: Int, onResult: (List<MusicInfo>) -> Unit) {
        viewModelScope.launch {
            runCatching {
                LxApp.app.musicRepository.getBoardList(source, bangId, page).list
            }.onSuccess(onResult)
                .onFailure { onResult(emptyList()) }
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
}
