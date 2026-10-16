package com.lxmusic.mobile.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.player.PlayMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 播放页：封面、歌词、控制按钮 */
@Composable
fun PlayerScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by LxApp.app.playerRepository.state.collectAsState()

    // 拖动进度条时，用本地值展示，避免被 500ms 的位置更新覆盖导致回弹
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1L).toFloat()
    val shownPosition = dragValue ?: state.positionMs.toFloat()

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("正在播放", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(12.dp))

        // 歌曲信息
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    state.current?.name ?: "未在播放",
                    fontSize = MiuixTheme.textStyles.main.fontSize,
                )
                Text(
                    state.current?.singer ?: "",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        // 歌词（滚动到当前行）
        LyricView(state, Modifier.weight(1f))

        // 进度条
        Slider(
            value = shownPosition,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { viewModel.seekTo(it.toLong()) }
                dragValue = null
            },
            valueRange = 0f..duration,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(viewModel.formatTime(shownPosition.toLong()), fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.weight(1f))
            Text(viewModel.formatTime(state.durationMs), fontSize = MiuixTheme.textStyles.body2.fontSize)
        }

        state.error?.let { err ->
            Text(
                err,
                color = MiuixTheme.colorScheme.error,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
        }

        // 控制按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            IconButtonWrap(MiuixIcons.Back, onClick = { viewModel.prev() })
            Spacer(Modifier.size(16.dp))
            IconButtonWrap(
                if (state.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                onClick = { viewModel.togglePlay() },
            )
            Spacer(Modifier.size(16.dp))
            IconButtonWrap(MiuixIcons.Forward, onClick = { viewModel.next() })
        }

        // 播放模式
        Row {
            PlayMode.ALL.forEach { mode ->
                Text(
                    PlayMode.label(mode),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.setMode(mode) },
                    color = if (state.playMode == mode) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }
        }
    }
}

/** 歌词视图：显示当前行并滚动跟随 */
@Composable
private fun LyricView(state: com.lxmusic.mobile.core.player.PlayState, modifier: Modifier = Modifier) {
    if (state.lyricLines.isEmpty()) {
        Text(
            "暂无歌词",
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = modifier,
        )
        return
    }
    val scrollState = rememberScrollState()
    val currentIndex = state.currentLyricIndex
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            // 让当前行大致居中
            scrollState.animateScrollTo(currentIndex * 40)
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.lyricLines.forEachIndexed { index, line ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Text(
                    line.text,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = if (index == currentIndex) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                line.translation?.let { tr ->
                    Text(
                        tr,
                        fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonWrap(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            ),
    )
}
