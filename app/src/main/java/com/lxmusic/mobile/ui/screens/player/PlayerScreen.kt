package com.lxmusic.mobile.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.player.PlayMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
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

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("正在播放", fontSize = MiuixTheme.textStyles.title.fontSize)
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
                    color = MiuixTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        // 歌词（滚动显示当前歌词行）
        Text(
            state.lyric.ifEmpty { "暂无歌词" },
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        // 进度条
        Slider(
            value = state.positionMs.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(viewModel.formatTime(state.positionMs), fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.weight(1f))
            Text(viewModel.formatTime(state.durationMs), fontSize = MiuixTheme.textStyles.body2.fontSize)
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
            listOf(
                PlayMode.LIST_LOOP to "列表循环",
                PlayMode.SINGLE_LOOP to "单曲循环",
                PlayMode.RANDOM to "随机播放",
                PlayMode.LIST to "顺序播放",
            ).forEach { (mode, label) ->
                Text(
                    label,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.setMode(mode) },
                    color = if (state.playMode == mode) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariant,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
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
