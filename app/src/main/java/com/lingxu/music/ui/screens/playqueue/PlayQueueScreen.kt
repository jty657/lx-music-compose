package com.lingxu.music.ui.screens.playqueue

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
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
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.PlayingIndicator
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    // 只订阅本页真正用到的三项（队列 / 当前下标 / 是否播放中），而不是整个 PlayState。
    // 整对象订阅会让本页所有行在播放时每 500ms（positionMs 轮询）重组一次，
    // 包括每行的 TrackCover 与图标 —— 队列长时滚动明显掉帧。
    // 拆成三项后，只有切歌/播放暂停/队列变化才会重组。
    val queue by remember { LxApp.app.playerRepository.state.map { it.queue }.distinctUntilChanged() }
        .collectAsState(initial = LxApp.app.playerRepository.state.value.queue)
    val currentIndex by remember { LxApp.app.playerRepository.state.map { it.queueIndex }.distinctUntilChanged() }
        .collectAsState(initial = LxApp.app.playerRepository.state.value.queueIndex)
    val isPlaying by remember { LxApp.app.playerRepository.state.map { it.isPlaying }.distinctUntilChanged() }
        .collectAsState(initial = LxApp.app.playerRepository.state.value.isPlaying)
    var showClearConfirm by remember { mutableStateOf(false) }
    // 长按歌曲弹出的操作菜单目标（null = 未打开）
    var actionTarget by remember { mutableStateOf<MusicInfo?>(null) }
    // 副标题字段由设置开关决定（对齐原版 list.isShowSource / isShowInterval / isShowAlbumName），
    // 与收藏页/搜索页等其它列表页一致。此前本页固定显示「歌手 · 来源」，
    // 用户在设置里关掉「显示来源/时长/专辑名」后队列页仍强制显示，属全站唯一未接入该设置的列表页。
    val showSource by LxApp.app.settings.isShowSource.collectAsState(initial = true)
    val showInterval by LxApp.app.settings.isShowInterval.collectAsState(initial = true)
    val showAlbumName by LxApp.app.settings.isShowAlbumName.collectAsState(initial = false)

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Dimens.PageH, vertical = 10.dp)) {
            BackIconButton(onClick = { navigator.pop() })
            Text(
                "播放队列（${queue.size}）",
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(start = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            if (queue.isNotEmpty()) {
                ClearQueueButton(onClick = { showClearConfirm = true })
            }
        }
        }
        Column(
            Modifier
                .fillMaxSize()
                // 大屏宽度上限：内容不再被拉伸到 1000dp+，手机端完全无变化
                .widthIn(max = ContentMaxWidth)
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
                    modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                    // 当前曲目整卡用主色淡染 + 主色描边（对应原版播放列表的「正在播放」底色），
                    // 此前只把歌名染成主色，一行里 3 个元素都是黑色文字，扫视时很难定位。
                    tint = if (index == currentIndex) {
                        MiuixTheme.colorScheme.primary.copy(alpha = 0.10f)
                    } else {
                        null
                    },
                    onClick = { viewModel.playAt(index) },
                    // 长按弹出歌曲操作菜单：补上「复制歌名」「歌曲详情」等队列页缺失的入口
                    onLongClick = { actionTarget = music },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        if (index == currentIndex) {
                            // 当前曲目：三条律动音柱（暂停时静帧），比单纯染色更易识别
                            PlayingIndicator(
                                playing = isPlaying,
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
                        TrackCover(url = music.meta.picUrl, size = Dimens.CoverRow, corner = Dimens.CornerS)
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                music.name,
                                fontSize = MiuixTheme.textStyles.main.fontSize,
                                color = if (index == currentIndex) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            // 副标题组装与收藏页 FavoriteCard / 搜索页 SearchResultCard 完全一致：
                            // 歌手恒显示，专辑名/来源/时长按设置开关按需拼接，空片段过滤掉悬空的「 · 」。
                            val subtitle = buildList {
                                add(music.singer)
                                if (showAlbumName) music.meta.albumName.takeIf { it.isNotBlank() }?.let { add(it) }
                                if (showSource) add(SourceConst.displayName(music.source))
                                if (showInterval) music.interval?.let { add(it) }
                            }.filter { it.isNotBlank() }
                            Text(
                                subtitle.joinToString(" · "),
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
                                    .queueActionClickable(enabled = index > 0) { viewModel.moveUp(index) },
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
                                    .queueActionClickable(enabled = index < queue.lastIndex) { viewModel.moveDown(index) },
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
                                .queueActionClickable { viewModel.removeAt(index) },
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

        // 长按弹出的歌曲操作菜单：播放走 playAt(当前下标)，与点击行为一致
        actionTarget?.let { music ->
            com.lingxu.music.ui.components.SongActionMenu(
                music = music,
                onPlay = { viewModel.playAt(queue.indexOfFirst { it.id == music.id }.coerceAtLeast(0)) },
                onDismiss = { actionTarget = null },
            )
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

/**
 * 顶栏「清空」文字键：与全站图标键同一手感——48dp 命中区 + 按压缩放反馈。
 *
 * 此前这里是裸 [clickable]（默认涟漪在 48dp 方形热区里会画成方角，和文字不搭），
 * 且是全站唯一「点下去无缩放反馈」的可点元素。改为 indication=null + 轻微缩放，
 * 对齐 [BackIconButton] / [queueActionClickable] 的语言；减弱动效时退化为静帧。
 * 同时补 Role.Button 语义，读屏可识别为按钮（原先只是一段可点文字）。
 */
@Composable
private fun ClearQueueButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.9f else 1f,
        animationSpec = Motion.press(),
        label = "clearQueuePress",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                indication = null,
                interactionSource = interaction,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "清空",
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.error,
        )
    }
}

/**
 * 队列操作图标（上移/下移/移除）的点击修饰：48dp 热区由调用点的 size 提供，这里补按压缩放反馈。
 * 与收藏页删除键、我的列表删除键、全站 BackIconButton 同一手感：只动 transform（不触发重新布局），
 * 禁用态（首项上移/末项下移）不给出反馈，减弱动效时退化为静帧。
 * 此前本页三个操作图标是全站唯一「点下去毫无变化」的可点元素，其它同类按钮已在 ac3b207 补齐。
 */
@Composable
private fun Modifier.queueActionClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !reduceMotion) 0.85f else 1f,
        animationSpec = Motion.press(),
        label = "queueActionPress",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        // 与 ClearQueueButton / BackIconButton 一致补 Role.Button：
        // 内层 Icon 的 contentDescription（上移/下移/移除）读屏会报成「图像」，加 role 后才是「按钮」。
        .clickable(
            enabled = enabled,
            indication = null,
            interactionSource = interactionSource,
            role = Role.Button,
            onClick = onClick,
        )
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
