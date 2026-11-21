package com.lingxu.music.ui.screens.playqueue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.PlayingIndicator
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.Motion
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
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            BackIconButton(onClick = { navigator.pop() })
            Text(
                "播放队列（${queue.size}）",
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(start = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            if (queue.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { showClearConfirm = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "清空",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
            }
        }
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))

        if (queue.isEmpty()) {
            EmptyHint(icon = MiuixIcons.Play, text = "队列是空的")
            return@Column
        }

        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            // key 只用稳定的歌曲 id：带上 index 会在排序/移除后导致整表重组与封面重载
            itemsIndexed(queue, key = { _, item -> item.id }) { index, music ->
                GlassSurface(
                    // animateItem：上移/下移/移除时条目平滑让位，而不是瞬移
                    modifier = Modifier.animateItem(
                        fadeInSpec = Motion.itemFade(),
                        placementSpec = Motion.itemPlacement(),
                        fadeOutSpec = Motion.itemFade(),
                    ).fillMaxWidth().padding(vertical = Dimens.ItemGap),
                    // 当前曲目整卡用主色淡染 + 主色描边（对应原版播放列表的「正在播放」底色），
                    // 此前只把歌名染成主色，一行里 3 个元素都是黑色文字，扫视时很难定位。
                    tint = if (index == currentIndex) {
                        MiuixTheme.colorScheme.primary.copy(alpha = 0.10f)
                    } else {
                        null
                    },
                    onClick = { viewModel.playAt(index) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        if (index == currentIndex) {
                            // 当前曲目：三条律动音柱（暂停时静帧），比单纯染色更易识别
                            PlayingIndicator(
                                playing = state.isPlaying,
                                modifier = Modifier.size(24.dp),
                            )
                        } else {
                            Icon(
                                MiuixIcons.Play,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        Spacer(Modifier.size(10.dp))
                        TrackCover(url = music.meta.picUrl, size = 44.dp, corner = 8.dp)
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                music.name,
                                fontSize = MiuixTheme.textStyles.main.fontSize,
                                color = if (index == currentIndex) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${music.singer} · ${SourceConst.displayName(music.source)}",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // 上移/下移：用旋转的 Back 箭头替代裸文字，图标化后触摸区更大、语义更清晰
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // 图标本身保持 20dp，点击热区统一到 48dp（Material 无障碍最小目标）
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clickable(enabled = index > 0) { viewModel.moveUp(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    MiuixIcons.Back,
                                    contentDescription = "上移",
                                    tint = if (index > 0) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = 90f },
                                )
                            }
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clickable(enabled = index < queue.lastIndex) { viewModel.moveDown(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    MiuixIcons.Back,
                                    contentDescription = "下移",
                                    tint = if (index < queue.lastIndex) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = -90f },
                                )
                            }
                        }
                        Spacer(Modifier.size(4.dp))
                        Box(
                            Modifier
                                .size(48.dp)
                                .clickable { viewModel.removeAt(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                MiuixIcons.Delete,
                                contentDescription = "移除",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
        }

        if (showClearConfirm) {
            com.lingxu.music.ui.components.GlassDialog(onDismissRequest = { showClearConfirm = false }) {
                Column(Modifier.padding(horizontal = 4.dp)) {
                    Text("清空播放队列", fontSize = MiuixTheme.textStyles.title1.fontSize)
                    Spacer(Modifier.size(12.dp))
                    Text("将停止播放并清空当前队列", fontSize = MiuixTheme.textStyles.body2.fontSize)
                    Spacer(Modifier.size(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    ) {
                        top.yukonga.miuix.kmp.basic.Button(onClick = { showClearConfirm = false }) { Text("取消") }
                        Spacer(Modifier.size(8.dp))
                        top.yukonga.miuix.kmp.basic.Button(onClick = {
                            viewModel.clearQueue()
                            showClearConfirm = false
                        }) { Text("清空") }
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

    fun moveUp(index: Int) {
        LxApp.app.playerRepository.moveInQueue(index, index - 1)
    }

    fun moveDown(index: Int) {
        LxApp.app.playerRepository.moveInQueue(index, index + 1)
    }

    fun clearQueue() {
        viewModelScope.launch {
            runCatching { LxApp.app.playerRepository.clear() }
        }
    }
}
