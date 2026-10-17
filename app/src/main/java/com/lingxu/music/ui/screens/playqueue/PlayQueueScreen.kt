package com.lingxu.music.ui.screens.playqueue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 播放队列：展示当前队列，点击切歌，可移除 */
@Composable
fun PlayQueueScreen(viewModel: PlayQueueViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val state by LxApp.app.playerRepository.state.collectAsState()
    val queue = state.queue
    val currentIndex = state.queueIndex

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
                "播放队列（${queue.size}）",
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

        if (queue.isEmpty()) {
            Text(
                "队列是空的",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(queue, key = { index, item -> "$index-${item.id}" }) { index, music ->
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { viewModel.playAt(index) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        Icon(
                            MiuixIcons.Play,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (index == currentIndex) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                music.name,
                                fontSize = MiuixTheme.textStyles.main.fontSize,
                                color = if (index == currentIndex) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                            )
                            Text(
                                "${music.singer} · ${SourceConst.displayName(music.source)}",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        Icon(
                            MiuixIcons.Delete,
                            contentDescription = "移除",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { viewModel.removeAt(index) },
                        )
                    }
                }
            }
        }
        }
    }
}

class PlayQueueViewModel : ViewModel() {
    fun playAt(index: Int) {
        viewModelScope.launch {
            runCatching { LxApp.app.playerRepository.playAt(index) }
        }
    }

    fun removeAt(index: Int) {
        viewModelScope.launch {
            runCatching { LxApp.app.playerRepository.removeFromQueue(index) }
        }
    }
}
