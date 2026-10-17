package com.lingxu.music.ui.screens.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.player.LyricLine
import com.lingxu.music.core.player.PlayMode
import com.lingxu.music.core.player.PlayState
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val CoverCorner = 22.dp

/** 播放页：封面、歌词、控制按钮 */
@Composable
fun PlayerScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by LxApp.app.playerRepository.state.collectAsState()
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val lyricFontScale by LxApp.app.settings.lyricFontSize.collectAsState(initial = 1f)
    val zoomActiveLrc by LxApp.app.settings.isZoomActiveLrc.collectAsState(initial = false)
    val showLrcTranslation by LxApp.app.settings.showLyricTranslation.collectAsState(initial = true)
    val showLrcRoma by LxApp.app.settings.showLyricRoma.collectAsState(initial = false)
    val lyricAlign by LxApp.app.settings.lyricAlign.collectAsState(initial = "left")
    val favoriteIds by LxApp.app.favoritesRepository.favoriteIds.collectAsState(initial = emptySet())
    var showMore by remember { mutableStateOf(false) }

    // 拖动进度条时，用本地值展示，避免被 500ms 的位置更新覆盖导致回弹
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1L).toFloat()
    val shownPosition = dragValue ?: state.positionMs.toFloat()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // 封面：约屏宽 72%，短屏时按高度收一点，避免把歌词挤没
        val coverSize = (maxWidth * 0.72f).coerceAtMost(maxHeight * 0.42f)

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            // 切歌时封面淡入 + 轻微缩放，给「换了一首」一个明确的视觉反馈；
            // 用 id 做键，同一首歌刷新封面（如搜索结果 picUrl -> 兜底大图）不会闪一下
            key(state.current?.id) {
                var coverShown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { coverShown = true }
                val coverAlpha by animateFloatAsState(
                    targetValue = if (coverShown) 1f else 0f,
                    animationSpec = tween(durationMillis = 260),
                    label = "coverAlpha",
                )
                val coverScale by animateFloatAsState(
                    targetValue = if (coverShown) 1f else 0.94f,
                    animationSpec = tween(durationMillis = 260),
                    label = "coverScale",
                )
                // 播放中封面缓慢呼吸（±1.5%），暂停时停在 1.0，让「在不在播」一眼可辨
                val breathScale by animateFloatAsState(
                    targetValue = if (state.isPlaying) 1.015f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "coverBreath",
                )
                TrackCover(
                    // coverUrl 是经 coverLoader 兜底拉到的；未就绪时先用搜索结果里的 picUrl
                    url = state.coverUrl ?: state.current?.meta?.picUrl,
                    size = coverSize,
                    corner = CoverCorner,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = coverAlpha
                            scaleX = coverScale * breathScale
                            scaleY = coverScale * breathScale
                        }
                        .shadow(14.dp, RoundedCornerShape(CoverCorner)),
                )
            }

            Spacer(Modifier.height(18.dp))

            // 歌名/歌手：切歌时轻微上移淡入，和封面一起构成一组过渡
            key(state.current?.id) {
                var textShown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { textShown = true }
                val textAlpha by animateFloatAsState(
                    targetValue = if (textShown) 1f else 0f,
                    animationSpec = tween(durationMillis = 260, delayMillis = 40),
                    label = "textAlpha",
                )
                val textOffsetY by animateDpAsState(
                    targetValue = if (textShown) 0.dp else 10.dp,
                    animationSpec = tween(durationMillis = 260, delayMillis = 40),
                    label = "textOffsetY",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer { alpha = textAlpha }
                        .offset(y = textOffsetY),
                ) {
                    Text(
                        state.current?.name ?: "未在播放",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.current?.singer ?: "",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LyricView(
                state,
                Modifier.weight(1f).fillMaxWidth(),
                fontScale = lyricFontScale,
                zoomActive = zoomActiveLrc,
                showTranslation = showLrcTranslation,
                showRoma = showLrcRoma,
                centered = lyricAlign == "center",
                onSeek = { viewModel.seekTo(it) },
            )

            state.error?.let { err ->
                Text(
                    err,
                    color = MiuixTheme.colorScheme.error,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 进度 + 控制：整块玻璃面板
            GlassSurface(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 时间与滑块同一视觉块
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            viewModel.formatTime(shownPosition.toLong()),
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Slider(
                            value = shownPosition,
                            onValueChange = { dragValue = it },
                            onValueChangeFinished = {
                                dragValue?.let { viewModel.seekTo(it.toLong()) }
                                dragValue = null
                            },
                            valueRange = 0f..duration,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        Text(
                            viewModel.formatTime(state.durationMs),
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // 主控制：上一首 / 播放暂停 / 下一首
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerIconButton(MiuixIcons.Back, 32.dp, "上一首") { viewModel.prev() }
                        Spacer(Modifier.size(28.dp))
                        PlayerIconButton(
                            if (state.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                            64.dp,
                            if (state.isPlaying) "暂停" else "播放",
                        ) { viewModel.togglePlay() }
                        Spacer(Modifier.size(28.dp))
                        PlayerIconButton(MiuixIcons.Forward, 32.dp, "下一首") { viewModel.next() }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 次控制：收藏 / 队列 / 分享 / 设置
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        PlayerIconButton(
                            if (state.current?.id in favoriteIds) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                            26.dp,
                            "收藏",
                        ) {
                            state.current?.let { music ->
                                scope.launch { LxApp.app.favoritesRepository.toggle(music) }
                            }
                        }
                        PlayerIconButton(MiuixIcons.Layers, 26.dp, "播放队列") {
                            if (navigator.backStack.last() != Screen.PlayQueue) navigator.push(Screen.PlayQueue)
                        }
                        PlayerIconButton(MiuixIcons.Share, 26.dp, "分享") {
                            state.current?.let { music ->
                                scope.launch {
                                    val type = LxApp.app.settings.shareType.first()
                                    LxApp.app.shareRepository.share(music, type)
                                }
                            }
                        }
                        PlayerIconButton(MiuixIcons.Settings, 26.dp, "播放设置") { showMore = true }
                    }

                    Spacer(Modifier.height(8.dp))

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
        }

        // 倍速/音量/定时（对应原版播放页 SettingPopup）
        if (showMore) {
            PlayerMoreSettings(
                onDismiss = { showMore = false },
                onTimeout = { minutes -> viewModel.setTimeoutExit(minutes) },
            )
        }
    }
}

/**
 * 歌词视图：LazyColumn 承载，滚动到当前行。
 *
 * 首尾各放一个约为可视高度 40% 的占位 item，让首/末行也能停在居中位置；
 * 滚动时给一个负偏移，使当前行停在距顶部 edgePad 处（≈垂直居中），
 * 而不是被 animateScrollToItem 顶到列表顶部。
 */
@Composable
private fun LyricView(
    state: PlayState,
    modifier: Modifier = Modifier,
    fontScale: Float = 1f,
    zoomActive: Boolean = false,
    showTranslation: Boolean = true,
    showRoma: Boolean = false,
    centered: Boolean = false,
    onSeek: (Long) -> Unit = {},
) {
    val horizontal = if (centered) Alignment.CenterHorizontally else Alignment.Start
    if (state.lyricLines.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "暂无歌词",
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        return
    }
    val listState = rememberLazyListState()
    val currentIndex = state.currentLyricIndex
    val baseSp = (MiuixTheme.textStyles.body2.fontSize.value * fontScale).sp

    BoxWithConstraints(modifier) {
        val edgePad = maxHeight * 0.4f
        val edgePadPx = with(LocalDensity.current) { edgePad.roundToPx() }
        // 用弹簧缓动替换默认的瞬时滚动：歌词跟着唱「滑」到位，而不是一格一格跳
        LaunchedEffect(currentIndex, edgePadPx, state.current?.id) {
            if (currentIndex >= 0) {
                // +1 跳过首部占位 item
                listState.animateScrollToItem(currentIndex + 1, scrollOffset = -edgePadPx)
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = horizontal,
        ) {
            item { Spacer(Modifier.height(edgePad)) }
            items(count = state.lyricLines.size) { index ->
                LyricLineRow(
                    line = state.lyricLines[index],
                    isCurrent = index == currentIndex,
                    baseSp = baseSp,
                    zoomActive = zoomActive,
                    showRoma = showRoma,
                    showTranslation = showTranslation,
                    horizontal = horizontal,
                    onSeek = onSeek,
                )
            }
            item { Spacer(Modifier.height(edgePad)) }
        }
    }
}

/** 单行歌词：当前行用动画过渡到高亮色与放大字号 */
@Composable
private fun LyricLineRow(
    line: LyricLine,
    isCurrent: Boolean,
    baseSp: TextUnit,
    zoomActive: Boolean,
    showRoma: Boolean,
    showTranslation: Boolean,
    horizontal: Alignment.Horizontal,
    onSeek: (Long) -> Unit,
) {
    val color by animateColorAsState(
        targetValue = if (isCurrent) MiuixTheme.colorScheme.primary
        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        animationSpec = tween(260),
        label = "lyricColor",
    )
    // 非当前行半透明、当前行全亮，配合颜色与缩放形成层次
    val lineAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.55f,
        animationSpec = tween(260),
        label = "lyricAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (zoomActive && isCurrent) 1.4f else 1f,
        animationSpec = tween(260),
        label = "lyricScale",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = lineAlpha }
            // 点歌词跳到对应时间（对应原版「点歌词调进度」）
            .clickable { onSeek(line.timeMs) }
            .padding(vertical = 5.dp),
        horizontalAlignment = horizontal,
    ) {
        Text(
            line.text,
            fontSize = baseSp * scale,
            color = color,
        )
        if (showRoma) {
            line.roma?.let { roma ->
                Text(
                    roma,
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        if (showTranslation) {
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

/** 播放页图标按钮：去掉水波纹，只保留点击热区 */
@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    size: Dp,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // Miuix 的 Icon 没有按压态，这里自己做：按下缩到 0.88，松手弹回，给按钮明确的触感
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "btnScale",
    )
    Icon(
        icon,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                indication = null,
                interactionSource = interaction,
                onClick = onClick,
            ),
    )
}

/** 播放页更多设置：倍速 / 音量 / 定时停止（对应原版播放页 SettingPopup） */
@Composable
private fun PlayerMoreSettings(
    onDismiss: () -> Unit,
    onTimeout: (Int) -> Unit,
) {
    val rate by LxApp.app.settings.playbackRate.collectAsState(initial = 1f)
    val volume by LxApp.app.settings.volume.collectAsState(initial = 1f)
    val viewModel: PlayerViewModel = viewModel()
    // 滑块用本地态：每次 onValueChange 都写 DataStore 会让显示值等回写，拖动时抖动/回弹
    var localRate by remember(rate) { mutableStateOf(rate) }
    var localVolume by remember(volume) { mutableStateOf(volume) }
    GlassDialog(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 4.dp)) {
            Text("播放设置", fontSize = MiuixTheme.textStyles.title1.fontSize)
            Spacer(Modifier.size(12.dp))
            Text("播放倍速 ${(localRate * 100).toInt()}%", fontSize = MiuixTheme.textStyles.body2.fontSize)
            androidx.compose.material3.Slider(
                value = localRate,
                onValueChange = { localRate = it },
                onValueChangeFinished = { viewModel.setPlaybackRate(localRate) },
                valueRange = 0.5f..3f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
            Text("音量 ${(localVolume * 100).toInt()}%", fontSize = MiuixTheme.textStyles.body2.fontSize)
            androidx.compose.material3.Slider(
                value = localVolume,
                onValueChange = { localVolume = it },
                onValueChangeFinished = { viewModel.setVolume(localVolume) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
            Text("定时停止播放", fontSize = MiuixTheme.textStyles.body2.fontSize)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(15, 30, 60).forEach { min ->
                    Button(onClick = { onTimeout(min); onDismiss() }) { Text("${min}分") }
                }
                Button(onClick = { onTimeout(0); onDismiss() }) { Text("关闭") }
            }
            Spacer(Modifier.size(12.dp))
            // 「添加到播放队列」此前没有任何入口（PlayerRepository.addToQueue 无调用方）
            val playerState by LxApp.app.playerRepository.state.collectAsState()
            Button(onClick = {
                playerState.current?.let { LxApp.app.playerRepository.addToQueue(it) }
                onDismiss()
            }) { Text("加入播放队列") }
        }
    }
}
