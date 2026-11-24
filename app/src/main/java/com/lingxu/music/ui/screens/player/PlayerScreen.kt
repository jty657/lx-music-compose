package com.lingxu.music.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.player.LyricLine
import com.lingxu.music.core.player.PlayMode
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.TextActionButton
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import com.lingxu.music.ui.theme.ChipShape
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.MotionFastMs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
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
    // 原版 playDetail.isShowLyricProgressSetting：手动滚动歌词时的逐句定位线
    val showLyricProgress by LxApp.app.settings.showLyricProgress.collectAsState(initial = false)
    // 原版 theme.fontShadow：歌词文字投影
    val lyricFontShadow by LxApp.app.settings.lyricFontShadow.collectAsState(initial = false)
    val favoriteIds by LxApp.app.favoritesRepository.favoriteIds.collectAsState(initial = emptySet())
    val reduceMotion = LocalReduceMotion.current
    var showMore by remember { mutableStateOf(false) }
    // 手动切到下一首且发生回绕时的轻提示（对齐原版 toast「已回到列表开头」的语义）。
    // 仅在用户主动点「下一首」时出现，自动切歌不打扰。
    var wrappedTip by remember { mutableStateOf(false) }
    LaunchedEffect(wrappedTip) {
        if (wrappedTip) {
            kotlinx.coroutines.delay(1600)
            wrappedTip = false
        }
    }
    // 下一首统一入口：回绕时给一次轻提示
    val goNext: () -> Unit = {
        if (viewModel.next()) wrappedTip = true
    }
    // 播放模式行横向滚动状态：只用于「右侧是否还有内容」的渐隐提示
    val modeScrollState = rememberScrollState()

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
                // 减弱动效时初值即为「已显示」，避免首帧先把封面压到透明/缩小再弹回的闪烁
                var coverShown by remember { mutableStateOf(reduceMotion) }
                LaunchedEffect(Unit) { coverShown = true }
                val coverDuration = if (reduceMotion) 0 else 260
                val coverAlpha by animateFloatAsState(
                    targetValue = if (coverShown) 1f else 0f,
                    animationSpec = tween(durationMillis = coverDuration),
                    label = "coverAlpha",
                )
                val coverScale by animateFloatAsState(
                    targetValue = if (coverShown) 1f else 0.94f,
                    animationSpec = tween(durationMillis = coverDuration),
                    label = "coverScale",
                )
                // 播放中封面缓慢呼吸（±1.5%），暂停时用有限动画平滑停在 1.0。
                // 暂停时若仍用 infiniteRepeatable 会一直空转重复，浪费每帧调度；
                // 系统要求减弱动效时同样不做无限循环，直接停在 1.0。
                val breathEnabled = state.isPlaying && !reduceMotion
                val breathScale by animateFloatAsState(
                    targetValue = if (breathEnabled) 1.015f else 1f,
                    animationSpec = if (breathEnabled) {
                        infiniteRepeatable(
                            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        )
                    } else {
                        tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    },
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
                        // 阴影颜色跟随主题：默认黑色阴影在深色背景上完全不可见，
                        // 封面像「贴」在背景上；深色下改用主色低透明度做「光晕」，浅色保持中性阴影。
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(CoverCorner),
                            ambientColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.35f),
                            spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f),
                        ),
                )
            }

            Spacer(Modifier.height(18.dp))

            // 歌名/歌手：切歌时轻微上移淡入，和封面一起构成一组过渡
            key(state.current?.id) {
                // 同封面：减弱动效时直接呈现最终态，去掉入场位移与延迟
                var textShown by remember { mutableStateOf(reduceMotion) }
                LaunchedEffect(Unit) { textShown = true }
                val textDuration = if (reduceMotion) 0 else 260
                val textDelay = if (reduceMotion) 0 else 40
                val textAlpha by animateFloatAsState(
                    targetValue = if (textShown) 1f else 0f,
                    animationSpec = tween(durationMillis = textDuration, delayMillis = textDelay),
                    label = "textAlpha",
                )
                val textOffsetY by animateDpAsState(
                    targetValue = if (textShown) 0.dp else 10.dp,
                    animationSpec = tween(durationMillis = textDuration, delayMillis = textDelay),
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

            // 回绕轻提示：队尾点「下一首」回到第一首时，在原位淡入一行小字，
            // 1.6s 后自动淡出（只动 alpha/位移，不挤动下方歌词）。
            androidx.compose.animation.AnimatedVisibility(
                visible = wrappedTip,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = tween(if (reduceMotion) 0 else 180),
                ) + androidx.compose.animation.slideInVertically(
                    animationSpec = tween(if (reduceMotion) 0 else 180),
                    initialOffsetY = { -it / 3 },
                ),
                exit = androidx.compose.animation.fadeOut(
                    animationSpec = tween(if (reduceMotion) 0 else 200),
                ),
            ) {
                Text(
                    "已回到列表开头",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .semantics { contentDescription = "已回到列表开头" },
                )
            }

            // 只传歌词视图真正依赖的值，而非整个 PlayState：positionMs 每 500ms 变化，
            // 传整个 state 会让歌词列表每 500ms 全量重组；拆分后只有当行号/歌曲变化时才重组。
            LyricView(
                lyricLines = state.lyricLines,
                currentIndex = state.currentLyricIndex,
                currentId = state.current?.id,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                fontScale = lyricFontScale,
                zoomActive = zoomActiveLrc,
                showTranslation = showLrcTranslation,
                showRoma = showLrcRoma,
                centered = lyricAlign == "center",
                showPlayLine = showLyricProgress,
                fontShadow = lyricFontShadow,
                reduceMotion = reduceMotion,
                onSeek = { viewModel.seekTo(it) },
            )

            state.error?.let { err ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        err,
                        color = MiuixTheme.colorScheme.error,
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    // 统一走 TextActionButton：48dp 命中区 + 按压缩放 + Role.Button + 静帧降级
                    TextActionButton(
                        text = "下一首",
                        onClick = goNext,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
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
                    // 主键改为 64dp 圆形主色底：此前它与其它图标键同形，只靠尺寸区分，
                    // 视觉重量不足以承担「播放页唯一焦点」。
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerIconButton(MiuixIcons.Back, 32.dp, "上一首") { viewModel.prev() }
                        Spacer(Modifier.size(20.dp))
                        PlayerMainButton(playing = state.isPlaying) { viewModel.togglePlay() }
                        Spacer(Modifier.size(20.dp))
                        PlayerIconButton(MiuixIcons.Forward, 32.dp, "下一首") { goNext() }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 次控制：收藏 / 队列 / 分享 / 设置
                    // 图标按钮热区已扩到 48dp（图标居中），故把间距从 28dp 收至 8dp，
                    // 保持与扩大前相近的视觉密度，避免按钮散开。
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PlayerIconButton(
                            if (state.current?.id in favoriteIds) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                            26.dp,
                            if (state.current?.id in favoriteIds) "取消收藏" else "收藏",
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

                    // 播放模式：5 个长文案（「列表循环播放」「禁用歌曲切换」…）横排总宽明显超过屏宽。
                    // 此前是可横向滚动的 Row，但没有任何「还能往右滑」的视觉提示，用户
                    // 不知道末尾还有「禁用歌曲切换」。这里在右侧叠一层与背景同色的渐隐，
                    // 只有真的能继续滚动时才显示（滚到尽头自动消失），遵循可发现性而无额外操作。
                    Box(Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(modeScrollState),
                        ) {
                            PlayMode.ALL.forEach { mode ->
                                val selected = state.playMode == mode
                                // 与搜索页 FilterChip 一致：按下时底色再加深一档，
                                // 让「已按下」在选中/未选中两种状态下都能被看见。
                                val modeInteraction = remember { MutableInteractionSource() }
                                val modePressed by modeInteraction.collectIsPressedAsState()
                                // 按压缩放：此前模式 chip 是播放页内唯一「按下只有底色变化、无缩放」的可点元素，
                                // 与同页收藏/队列/设置图标键的手感不一致。补一档轻微缩放（0.94），只动 transform，
                                // 幅度比图标键（0.88）更小——chip 横向密集排列，收缩过大会互相牵动显得乱。
                                // 接入 reduceMotion：系统要求减弱动效时退化为静帧，与底色过渡的降级一致。
                                val modeScale by animateFloatAsState(
                                    targetValue = if (modePressed && !reduceMotion) 0.94f else 1f,
                                    animationSpec = Motion.press(),
                                    label = "modeChipPress",
                                )
                                // 选中态底色随模式切换渐变，而不是硬切；按下态叠加一档加深
                                val modeBg by animateColorAsState(
                                    targetValue = when {
                                        selected -> MiuixTheme.colorScheme.primary.copy(
                                            alpha = if (modePressed) 0.24f else 0.14f,
                                        )
                                        modePressed -> MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.10f)
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    },
                                    // 减弱动效下选中底色瞬时到位，与同一 Box 内渐隐提示（下方
                                    // canScrollForward 的 fadeIn/fadeOut）的降级保持一致。
                                    animationSpec = tween(durationMillis = if (reduceMotion) 0 else MotionFastMs),
                                    label = "modeBg",
                                )
                                Text(
                                    PlayMode.label(mode),
                                    // 胶囊形：与搜索页的筛选 chip 保持同一形态语言
                                    modifier = Modifier
                                        // 按压缩放放在 clip 之前：缩放整个 chip（底色 + 文字），
                                        // 不被圆角裁剪；只动 transform，不触发重新布局。
                                        .graphicsLayer { scaleX = modeScale; scaleY = modeScale }
                                        // 命中区兜底 48dp：11dp padding 时文字高约 44dp，未达 Material 建议下限；
                                        // 与 FilterChip/RetryHint 一致，视觉仍是紧凑胶囊
                                        .minimumInteractiveComponentSize()
                                        // 用统一胶囊形状（ChipShape）：与搜索页 FilterChip 同一形态语言，
                                        // 兑现上方「胶囊形」注释；此前误用 CornerM(12dp) 显得像小按钮而非可选项。
                                        .clip(ChipShape)
                                        .background(modeBg)
                                        .clickable(
                                            interactionSource = modeInteraction,
                                            indication = null,
                                            role = Role.Tab,
                                            onClick = { viewModel.setMode(mode) },
                                        )
                                        // 与 FilterChip 对齐：读屏可播报「已选中/未选中」，避免只靠颜色区分选中态
                                        .semantics { this.selected = selected }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    color = if (selected) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                )
                            }
                        }
                        // 右侧渐隐：暗示「右边还有」。只在未滚到底时出现，滚到底后淡出，
                        // 避免常驻遮挡最后一个 chip。
                        val canScrollForward by remember {
                            derivedStateOf { modeScrollState.canScrollForward }
                        }
                        // 用 BoxScope 版本：外层是 Column，直接调 ColumnScope 重载会解析失败
                        androidx.compose.animation.AnimatedVisibility(
                            visible = canScrollForward,
                            modifier = Modifier.align(Alignment.CenterEnd),
                            enter = fadeIn(tween(if (reduceMotion) 0 else MotionFastMs)),
                            exit = fadeOut(tween(if (reduceMotion) 0 else MotionFastMs)),
                        ) {
                            Box(
                                Modifier
                                    .width(36.dp)
                                    .height(44.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            0f to androidx.compose.ui.graphics.Color.Transparent,
                                            1f to MiuixTheme.colorScheme.surface,
                                        ),
                                    ),
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

/** 逐句定位线的时间戳：与原版 formatPlayTime2 同一语义（M:SS），负时间钳到 0。 */
private fun formatLyricTime(timeMs: Long): String {
    val seconds = timeMs.coerceAtLeast(0L) / 1000L
    return "${seconds / 60L}:${(seconds % 60L).toString().padStart(2, '0')}"
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
    lyricLines: List<LyricLine>,
    currentIndex: Int,
    currentId: String?,
    modifier: Modifier = Modifier,
    fontScale: Float = 1f,
    zoomActive: Boolean = false,
    showTranslation: Boolean = true,
    showRoma: Boolean = false,
    centered: Boolean = false,
    showPlayLine: Boolean = false,
    fontShadow: Boolean = false,
    reduceMotion: Boolean = false,
    onSeek: (Long) -> Unit = {},
) {
    val horizontal = if (centered) Alignment.CenterHorizontally else Alignment.Start
    if (lyricLines.isEmpty()) {
        // 与全站空态统一：居中 + 占位图标 + 入场淡入上浮；此处是全站最后一处裸文字空态。
        // 图标沿用已导入的 Favorites（歌词/收藏语义相近），减弱动效时静帧。
        EmptyHint(icon = MiuixIcons.Favorites, text = "暂无歌词", modifier = modifier)
        return
    }
    val listState = rememberLazyListState()
    val baseSp = (MiuixTheme.textStyles.body2.fontSize.value * fontScale).sp

    // 自动跟随开关：用户手动触摸歌词列表时暂停跟随，避免每句切换都把正在回看的歌词拽回当前行。
    // 以 currentId 为 key：切到下一首时自动恢复跟随。点「回到当前歌词」也恢复。
    var followCurrent by remember(currentId) { mutableStateOf(true) }

    BoxWithConstraints(modifier) {
        val edgePad = maxHeight * 0.4f
        val edgePadPx = with(LocalDensity.current) { edgePad.roundToPx() }
        // 逐句定位线（对应原版 PlayLine）：手动回看时，取距顶部 40% 视线最近的一句歌词。
        // 只读 LazyList 的布局信息，不额外测量、不拦截手势；跟随自动滚动时不显示。
        val playLineIndex by remember(lyricLines, edgePadPx) {
            derivedStateOf {
                val info = listState.layoutInfo
                val focusY = info.viewportStartOffset + edgePadPx
                info.visibleItemsInfo
                    // LazyColumn 的第 0 项是首部占位，歌词下标 = item.index - 1
                    .filter { it.index in 1..lyricLines.size }
                    .minByOrNull { item -> abs((item.offset + item.size / 2f) - focusY) }
                    ?.index?.minus(1)
            }
        }
        val targetLine = playLineIndex?.takeIf { it in lyricLines.indices }
        // 用弹簧缓动替换默认的瞬时滚动：歌词跟着唱「滑」到位，而不是一格一格跳。
        // followCurrent 作为 key：恢复跟随时立即把当前行滑回居中位置。
        // 注意：animateScrollToItem 是协程动画，不受 Compose「动画时长缩放」影响 —— 系统
        // 开启「移除动画」时它仍会滑动。这里显式短路为瞬时跳转，与前庭无障碍要求一致。
        LaunchedEffect(currentIndex, edgePadPx, currentId, followCurrent) {
            if (currentIndex >= 0 && followCurrent) {
                // +1 跳过首部占位 item
                if (reduceMotion) {
                    listState.scrollToItem(currentIndex + 1, scrollOffset = -edgePadPx)
                } else {
                    listState.animateScrollToItem(currentIndex + 1, scrollOffset = -edgePadPx)
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                // 只观察 touch down、不消费手势，LazyColumn 自身的滚动/点击不受影响。
                // 一旦用户手指按下歌词区，说明想手动回看，暂停自动跟随。
                .pointerInput(currentId) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        followCurrent = false
                        // 等本次手势的所有手指抬起，期间不重复置位
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    }
                },
            horizontalAlignment = horizontal,
        ) {
            item { Spacer(Modifier.height(edgePad)) }
            items(count = lyricLines.size) { index ->
                LyricLineRow(
                    line = lyricLines[index],
                    isCurrent = index == currentIndex,
                    baseSp = baseSp,
                    zoomActive = zoomActive,
                    showRoma = showRoma,
                    showTranslation = showTranslation,
                    horizontal = horizontal,
                    fontShadow = fontShadow,
                    reduceMotion = reduceMotion,
                    onSeek = { timeMs ->
                        // 点歌词跳转是主动定位：恢复自动跟随，让列表跟着新的播放位置继续走
                        followCurrent = true
                        onSeek(timeMs)
                    },
                )
            }
            item { Spacer(Modifier.height(edgePad)) }
        }

        // 原版 PlayLine：手动回看时叠加一条虚线刻度 + 该句时间戳 + 跳转按钮。
        // 只动 alpha 淡入淡出（transform/opacity 类），不触发列表重新布局；
        // 系统要求减弱动效时瞬时到位，避免滚动回看时反复淡变干扰阅读。
        AnimatedVisibility(
            visible = showPlayLine && !followCurrent && targetLine != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = edgePad),
            enter = fadeIn(tween(if (reduceMotion) 0 else 180)),
            exit = fadeOut(tween(if (reduceMotion) 0 else 120)),
        ) {
            val lineIndex = targetLine
            if (lineIndex != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 12.dp),
                ) {
                    // 虚线：与原版 dashed border 相同的视觉语言，但用 drawBehind 绘制，
                    // 不增加一个占位的布局节点。颜色需在 Composable 作用域先取出，
                    // drawBehind 的绘制 lambda 内不能读 MiuixTheme 的 CompositionLocal。
                    val lineColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.55f)
                    Box(
                        Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .height(1.dp)
                            .drawBehind {
                                drawLine(
                                    color = lineColor,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                                )
                            },
                    )
                    Row(
                        Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            formatLyricTime(lyricLines[lineIndex].timeMs),
                            fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                            color = MiuixTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(8.dp))
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.14f))
                                .clickable(role = Role.Button, onClickLabel = "跳转到该句歌词") {
                                    // 与原版 handlePlayLine 一致：跳到该句并恢复自动跟随。
                                    followCurrent = true
                                    onSeek(lyricLines[lineIndex].timeMs)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                MiuixIcons.Play,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        // 手动回看时浮出「回到当前歌词」入口，点击后恢复自动跟随并把当前行滑回居中。
        // 放在 LazyColumn 之后（BoxWithConstraints 内叠层），不占用列表布局、不遮挡歌词。
        // 用 AnimatedVisibility 淡入上浮，避免回看时按钮突然出现打断阅读。
        // 减弱动效时淡入与位移都瞬时到位，避免按钮上浮干扰前庭敏感用户。
        AnimatedVisibility(
            visible = !followCurrent && currentIndex >= 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            enter = fadeIn(tween(if (reduceMotion) 0 else 180)) +
                slideInVertically(tween(if (reduceMotion) 0 else 180)) { it / 2 },
            exit = fadeOut(tween(if (reduceMotion) 0 else 120)) +
                slideOutVertically(tween(if (reduceMotion) 0 else 120)) { it / 2 },
        ) {
            GlassSurface(
                // 补 Role.Button：该浮层是「回到当前歌词」的可点入口，读屏可识别为按钮。
                // 通过 GlassSurface 的 role 参数传入（而非外部 semantics，避免与内部 clickable
                // 语义节点分离），点击与入场动效均不变。
                role = Role.Button,
                shape = RoundedCornerShape(Dimens.CardCorner),
                onClick = { followCurrent = true },
            ) {
                Text(
                    "回到当前歌词",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MiuixTheme.colorScheme.primary,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }
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
    fontShadow: Boolean = false,
    reduceMotion: Boolean = false,
    onSeek: (Long) -> Unit,
) {
    // 歌词行高亮过渡在减弱动效时瞬时到位：颜色、透明度随系统开关一起降级，
    // 尤其是 1.4x 的当前行缩放（每切一句歌词都会触发、transform 类动画），
    // 对前庭敏感用户是持续可见的位移，应与同文件封面/按钮动画保持一致。
    val lyricDuration = if (reduceMotion) 0 else 260
    val color by animateColorAsState(
        targetValue = if (isCurrent) MiuixTheme.colorScheme.primary
        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        animationSpec = tween(lyricDuration),
        label = "lyricColor",
    )
    // 非当前行半透明、当前行全亮，配合颜色与缩放形成层次
    val lineAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.55f,
        animationSpec = tween(lyricDuration),
        label = "lyricAlpha",
    )
    // 当前行放大：用 graphicsLayer 的 transform 而不是改 fontSize。
    // 改字号会让行高随动画逐帧变化 → LazyColumn 反复重新测量，切行时整列表抖动，
    // 大字号 + 长歌词下尤其明显。transform 只影响绘制，不触发布局。
    // transformOrigin 跟随对齐方向，避免居/左对齐时放大内容从中心外溢。
    val scale by animateFloatAsState(
        targetValue = if (zoomActive && isCurrent) 1.4f else 1f,
        animationSpec = tween(lyricDuration),
        label = "lyricScale",
    )
    val originX = if (horizontal == Alignment.CenterHorizontally) 0.5f else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = lineAlpha
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(originX, 0.5f)
            }
            // 点歌词跳到对应时间（对应原版「点歌词调进度」）
            // 补 Role.Button 语义，读屏可识别为可操作的跳转项而非普通文本
            .clickable(role = Role.Button, onClickLabel = "跳转到该句歌词") { onSeek(line.timeMs) }
            .padding(vertical = 5.dp),
        horizontalAlignment = horizontal,
    ) {
        Text(
            line.text,
            fontSize = baseSp,
            color = color,
            // 原版 theme.fontShadow：给歌词文字一层轻投影，浅色封面/壁纸下更清晰。
            // 只改绘制样式，不改字号与行高，因此不会引起列表重新测量。
            style = if (fontShadow) {
                androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                        blurRadius = 4f,
                    ),
                )
            } else {
                androidx.compose.ui.text.TextStyle.Default
            },
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

/** 播放页图标按钮：去掉水波纹，只保留点击热区；热区最小 48dp，视觉尺寸不变 */
@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    size: Dp,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // Miuix 的 Icon 没有按压态，这里自己做：按下缩到 0.88，松手弹回，给按钮明确的触感。
    // 与 PlayerMainButton / BackIconButton 一致：减弱动效时退化为静帧，不做缩放。
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.88f else 1f,
        animationSpec = Motion.press(),
        label = "btnScale",
    )
    // 热区至少 48dp（Material 无障碍最小目标）；主播放键本身 64dp 则保持原尺寸。
    // 用 Box 承载点击与缩放动画，Icon 只用 size 控制视觉大小，二者解耦。
    val hitSize = if (size >= 48.dp) size else 48.dp
    Box(
        modifier = Modifier
            .size(hitSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                indication = null,
                interactionSource = interaction,
                // 与 BackIconButton / TextActionButton 一致补 Role.Button：
                // 内层 Icon 的 contentDescription 会被读屏报成「图像」，加 role 后才是「按钮」，
                // 视障用户可正确识别这些播放控制键。仅语义变化，行为不变。
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 播放/暂停图标淡入淡出切换，避免同一位置硬切造成「闪一下」
        Crossfade(
            targetState = icon,
            // 减弱动效下瞬时切换（静帧降级）；图标淡变是纯装饰，不需要保留
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else 180),
            label = "playerIcon",
        ) { shownIcon ->
            Icon(shownIcon, contentDescription = contentDescription, modifier = Modifier.size(size))
        }
    }
}

/**
 * 主播放 / 暂停键：64dp 圆形主色底 + 反色图标，按下时整圆缩放并加深底色。
 *
 * 与次级图标键的 0.88 缩放形成层次（主键幅度更大、底色变深），
 * 系统要求减弱动效时只保留颜色变化，不做缩放。
 */
@Composable
private fun PlayerMainButton(playing: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.92f else 1f,
        // 主播放键属于「低频、明显」档，沿用原 420 刚度的 emphasized（见 Motion.kt 分档定义）
        animationSpec = Motion.emphasized(),
        label = "mainBtnScale",
    )
    val bg by animateColorAsState(
        targetValue = if (pressed) {
            MiuixTheme.colorScheme.primary.copy(alpha = 0.82f)
        } else {
            MiuixTheme.colorScheme.primary
        },
        // 减弱动效下底色瞬时到位，与同函数按压缩放的静帧降级保持一致
        // 160ms 等值收敛到 MotionFastMs（与全站短交互时长统一，数值不变）
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else MotionFastMs),
        label = "mainBtnBg",
    )
    Box(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bg)
            .clickable(
                indication = null,
                interactionSource = interaction,
                // 主播放键同样补 Role.Button，读屏识别为按钮（同 PlayerIconButton）
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = playing,
            // 减弱动效下瞬时切换（静帧降级），与主键缩放/底色的降级一致
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else 180),
            label = "mainBtnIcon",
        ) { isPlaying ->
            Icon(
                if (isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = MiuixTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
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
            Slider(
                value = localRate,
                onValueChange = { localRate = it },
                onValueChangeFinished = { viewModel.setPlaybackRate(localRate) },
                valueRange = 0.5f..3f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
            Text("音量 ${(localVolume * 100).toInt()}%", fontSize = MiuixTheme.textStyles.body2.fontSize)
            Slider(
                value = localVolume,
                onValueChange = { localVolume = it },
                onValueChangeFinished = { viewModel.setVolume(localVolume) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
            Text("定时停止播放", fontSize = MiuixTheme.textStyles.body2.fontSize)
            // 窄屏 / 大字号下四个按钮裸横排会超出屏宽被裁切，末尾「关闭」无法点到。
            // 与播放模式分段一致改为横向可滚动；正常宽度下不产生滚动，观感不变。
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                listOf(15, 30, 60).forEach { min ->
                    Button(onClick = { onTimeout(min); onDismiss() }) { Text("${min}分") }
                }
                Button(onClick = { onTimeout(0); onDismiss() }) { Text("关闭") }
            }
            Spacer(Modifier.size(12.dp))
            // 原「加入播放队列」把正在播放的歌入队（它本就在队列里，addToQueue 去重后为空操作），
            // 属无效交互。改为「复制歌曲信息」，给用户一个实际有用的操作。
            val ctx = LocalContext.current
            val playerState by LxApp.app.playerRepository.state.collectAsState()
            Button(onClick = {
                playerState.current?.let { music ->
                    val text = "${music.name} - ${music.singer}"
                    val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("歌曲信息", text))
                }
                onDismiss()
            }) { Text("复制歌曲信息") }
        }
    }
}
