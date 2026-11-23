package com.lingxu.music.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.lingxu.music.LxApp
import com.lingxu.music.ui.components.GlassContext
import com.lingxu.music.ui.components.GlassHost
import com.lingxu.music.ui.components.LocalGlass
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalBottomBarHeight
import com.lingxu.music.ui.navigation.LocalMiniBarHeight
import com.lingxu.music.ui.navigation.LocalMiniBarScroll
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Navigator
import com.lingxu.music.ui.navigation.Screen
import com.lingxu.music.ui.navigation.rememberNavigator
import com.lingxu.music.ui.screens.favorite.FavoriteScreen
import com.lingxu.music.ui.screens.board.BoardDetailScreen
import com.lingxu.music.ui.screens.home.HomeScreen
import com.lingxu.music.ui.screens.mylist.MyListsScreen
import com.lingxu.music.ui.screens.songlist.SongListDetailScreen
import com.lingxu.music.ui.screens.songlist.SongListScreen
import com.lingxu.music.ui.screens.player.PlayerScreen
import com.lingxu.music.ui.screens.search.SearchScreen
import com.lingxu.music.ui.screens.setting.SettingScreen
import com.lingxu.music.ui.screens.source.SourceScreen
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LxTheme
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.isInDarkTheme
import com.lingxu.music.ui.theme.motionMs
import com.lingxu.music.ui.theme.rememberReduceMotion
import kotlinx.coroutines.flow.map
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppRoot() {
    val themeMode by LxApp.app.settings.theme.collectAsState(initial = "system")
    val fontScale by LxApp.app.settings.fontScale.collectAsState(initial = 1f)
    val blurSettingEnabled by LxApp.app.settings.blurEnabled.collectAsState(initial = true)
    val blurRadius by LxApp.app.settings.blurRadius.collectAsState(initial = 24f)
    // RenderEffect 需 API 31+；低版本上 backdrop 的 blur 是静默 no-op，只会得到半透明色块，
    // 因此这里直接降级为普通底栏，观感与「关闭模糊」一致
    val renderEffectSupported = android.os.Build.VERSION.SDK_INT >= 31
    val runtimeShaderSupported = android.os.Build.VERSION.SDK_INT >= 33
    val blurEnabled = blurSettingEnabled && renderEffectSupported

    LxTheme(themeMode = themeMode, fontScale = fontScale) {
        val navigator = rememberNavigator()
        // 原版 player.startupPushPlayDetailScreen：开启且有上次播放记录时，冷启动直接进入播放页。
        // 只在本次冷启动的首次组合里执行一次（startupNavDone 守护），且不覆盖用户已手动导航的状态：
        // 仅当仍在根页 Home 时才 push，避免把用户从其它页面拽回来。上次播放恢复是异步的，
        // 故用 state.map 派生「是否已有 current」并观察，恢复完成后满足条件再进入。
        val startupPushDetail by LxApp.app.settings.startupPushPlayDetail.collectAsState(initial = false)
        val hasLastPlay by remember {
            LxApp.app.playerRepository.state.map { it.current != null }
        }.collectAsState(initial = false)
        val startupNavDone = remember { mutableStateOf(false) }
        LaunchedEffect(startupPushDetail, hasLastPlay) {
            if (!startupNavDone.value && startupPushDetail && hasLastPlay &&
                navigator.backStack.size == 1 && navigator.backStack.last() == Screen.Home
            ) {
                startupNavDone.value = true
                navigator.push(Screen.Player)
            }
        }
        val dark = isInDarkTheme()
        // 系统「动画时长缩放 = 关闭」时降级为静帧动效（无障碍 + 低端机省电）
        val reduceMotion = rememberReduceMotion()
        // 状态栏图标极性跟随应用实际主题：深色主题用浅色图标，避免深色图标压在深色内容上不可读。
        // enableEdgeToEdge 默认按 XML 的浅色主题推断，这里按运行时主题纠正（含用户 App 内切换）。
        val context = LocalContext.current
        LaunchedEffect(dark) {
            val window = (context as? android.app.Activity)?.window ?: return@LaunchedEffect
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = !dark
        }
        val autoHidePlayBar by LxApp.app.settings.autoHidePlayBar.collectAsState(initial = true)
        // 列表向下滚动时隐藏迷你播放条（对应原版 common.autoHidePlayBar）
        val miniBarScroll = remember { mutableStateOf(0) }
        // 派生状态：直接由滚动量与开关推导，避免 LaunchedEffect 写回 state 造成的切换瞬间闪旧值与多余重组
        // autoHidePlayBar=false 的语义是「始终显示」，不是「始终隐藏」
        val miniBarVisible by remember(autoHidePlayBar) {
            derivedStateOf { !autoHidePlayBar || miniBarScroll.value <= 0 }
        }
        // 关闭模糊或系统不支持 RenderEffect（API < 31）时，radius 传 0 让玻璃面变成纯 tint
        val effectiveRadius = if (blurEnabled) blurRadius else 0f
        // 底栏/迷你条实测高度：初始值取常规字号下的经验值，测量后立刻被覆盖，
        // 保证首帧不会因为 0dp 而抖动一下。
        val bottomBarHeight = remember { mutableStateOf(76.dp) }
        val miniBarHeight = remember { mutableStateOf(56.dp) }
        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalMiniBarScroll provides miniBarScroll,
            LocalReduceMotion provides reduceMotion,
            LocalBottomBarHeight provides bottomBarHeight.value,
            LocalMiniBarHeight provides miniBarHeight.value,
        ) {
            GlassHost(
                dark = dark,
                radius = effectiveRadius,
                refraction = runtimeShaderSupported && blurEnabled,
            ) { glass ->
                Box(Modifier.fillMaxSize()) {
                    // 主内容（被玻璃模糊的目标）。layerBackdrop 只套在这里，
                    // 底栏/迷你播放条必须是它的兄弟节点，否则 drawBackdrop 会自采样导致闪退
                    MainScaffold(
                        navigator = navigator,
                        modifier = Modifier.fillMaxSize().layerBackdrop(glass.content),
                    )

                    // 底部：迷你播放条 + 导航栏
                    // 切页时复位滚动量，否则在 A 页滑下隐藏后进 B 页会一直不显示
                    val currentScreen = navigator.backStack.last()
                    // 播放页是全屏沉浸页，自带完整播放控制，不再叠加迷你条与底栏
                    val isPlayerScreen = currentScreen == Screen.Player
                    LaunchedEffect(currentScreen) { miniBarScroll.value = 0 }
                    if (!isPlayerScreen) {
                        Column(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding(),
                        ) {
                            AnimatedVisibility(
                                visible = miniBarVisible,
                                // 迷你条滑入/淡入是全站最后一处未降级的 AnimatedVisibility：
                                // 正常模式完全保留原 spring 位移+淡变手感（零变化）；系统要求减弱动效时
                                // 改为瞬时淡变（tween 0）、去掉位移，与 Glass/PlayerScreen/MyLists 的
                                // AnimatedVisibility 降级约定一致，静帧到位不空播滑入动画。
                                enter = if (reduceMotion) fadeIn(tween(0)) else slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = if (reduceMotion) fadeOut(tween(0)) else slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            ) {
                                // 高度上报给 MainScaffold 预留底部留白（字体缩放后会变高）
                                val d = androidx.compose.ui.platform.LocalDensity.current
                                Box(Modifier.onSizeChanged { miniBarHeight.value = with(d) { it.height.toDp() } }) {
                                    MiniPlayerBar(navigator)
                                }
                            }
                            val dBar = androidx.compose.ui.platform.LocalDensity.current
                            Box(Modifier.onSizeChanged { bottomBarHeight.value = with(dBar) { it.height.toDp() } }) {
                                if (blurEnabled) {
                                    val glass = LocalGlass.current
                                    if (glass != null) {
                                        GlassBottomBar(glass, navigator)
                                    } else {
                                        PlainBottomBar(navigator)
                                    }
                                } else {
                                    PlainBottomBar(navigator)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        val current = navigator.backStack.last()
        val playState by LxApp.app.playerRepository.state.collectAsState()
        // 页面转场用：系统要求减弱动效时退化为纯淡入淡出
        val reduceMotion = LocalReduceMotion.current
        // 系统返回键：非根页时回退一级
        BackHandler(enabled = navigator.backStack.size > 1) {
            navigator.pop()
        }
        // 底部留白：播放页是全屏沉浸页，无需为底栏留白；
        // 其余页面为导航栏 + （有歌曲时的）迷你播放条预留空间。
        //
        // 此前写死 76dp / 132dp：底栏高度会随「字体缩放」（设置页可调到 150%）与 Miuix
        // NavigationBar 自身高度变化，迷你条的歌名/歌手两行也会撑高，固定值在大字号下
        // 会让最后一条列表项被底栏压住。改为读取实际测得的底栏/迷你条高度。
        val isPlayer = current == Screen.Player
        val bottomBarHeight = LocalBottomBarHeight.current
        val miniBarHeight = LocalMiniBarHeight.current
        val bottomInset = when {
            isPlayer -> 0.dp
            playState.current != null -> bottomBarHeight + miniBarHeight
            else -> bottomBarHeight
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomInset),
        ) {
            // 页面切换：位移 + 淡入，避免硬切换；
            // 返回时反向：上一页从左侧滑入、当前页向右滑出，让「前进/后退」的方向感一致。
            // 位移幅度取容器宽的 1/4，但封顶 96dp：纯比例在平板上等于 200dp+ 的大幅横扫，
            // 与手机端不是一个观感；封顶后大屏上是「轻微侧移 + 淡变」，小屏保留原有的方向感。
            // 缓动用 FastOutSlowIn 收尾更「跟手」；系统要求减弱动效时退化为纯淡入淡出，不做位移。
            val shiftPx = with(androidx.compose.ui.platform.LocalDensity.current) { 96.dp.roundToPx() }
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val back = navigator.lastWasPop
                    val shift = if (reduceMotion) 0 else shiftPx
                    val enter = fadeIn(tween(240.motionMs(reduceMotion), easing = FastOutSlowInEasing)) +
                        slideInHorizontally(tween(240.motionMs(reduceMotion), easing = FastOutSlowInEasing)) {
                            when {
                                shift == 0 -> 0
                                back -> -minOf(it / 4, shift)
                                else -> minOf(it / 4, shift)
                            }
                        }
                    val exit = fadeOut(tween(160.motionMs(reduceMotion))) +
                        slideOutHorizontally(tween(200.motionMs(reduceMotion), easing = FastOutSlowInEasing)) {
                            when {
                                shift == 0 -> 0
                                back -> minOf(it / 4, shift)
                                else -> -minOf(it / 4, shift)
                            }
                        }
                    enter togetherWith exit
                },
                label = "screen",
            ) { screen ->
                when (screen) {
                    is Screen.Home -> HomeScreen()
                    is Screen.Search -> SearchScreen(initialKeyword = screen.keyword)
                    is Screen.Source -> SourceScreen()
                    is Screen.Setting -> SettingScreen()
                    is Screen.Player -> PlayerScreen()
                    is Screen.Favorite -> FavoriteScreen()
                    is Screen.BoardList -> BoardDetailScreen(
                        source = screen.source,
                        bangId = screen.bangId,
                        name = screen.name,
                    )
                    is Screen.SongList -> SongListScreen(source = screen.source)
                    is Screen.SongListDetail -> com.lingxu.music.ui.screens.songlist.SongListDetailScreen(
                        source = screen.source,
                        id = screen.id,
                        name = screen.name,
                    )
                    is Screen.MyLists -> MyListsScreen()
                    is Screen.PlayQueue -> com.lingxu.music.ui.screens.playqueue.PlayQueueScreen()
                }
            }
        }
    }
}

/** 迷你播放条：显示当前歌曲并可进入播放页 */
@Composable
private fun MiniPlayerBar(navigator: Navigator) {
    val state by LxApp.app.playerRepository.state.collectAsState()
    val current = state.current ?: return
    val glass = LocalGlass.current
    val pieceShape = RoundedCornerShape(Dimens.CardCorner)
    val dark = glass?.dark ?: isInDarkTheme()

    // 整行「进入播放页」点击区与全站可点元素统一交互语言：此前这里是全站最后一处
    // 裸 clickable —— 系统方角涟漪画在圆角玻璃条上、按下无任何反馈、读屏只当普通容器。
    // 现在改为 indication=null + 轻微按压缩放 + Role.Button：只动 transform 不触发布局；
    // 幅度取 0.985（比紧凑图标键 0.9 更保守，避免整条大面积背景明显塌陷），
    // 减弱动效时退化为静帧。点击守卫与跳转逻辑完全不变。
    val barInteraction = remember { MutableInteractionSource() }
    val barPressed by barInteraction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val barScale by animateFloatAsState(
        targetValue = if (barPressed && !reduceMotion) 0.985f else 1f,
        animationSpec = Motion.press(),
        label = "miniPlayerBarPress",
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp)
            .then(
                if (glass != null && glass.radius > 0f) {
                    Modifier.drawBackdrop(
                        backdrop = glass.content,
                        shape = { pieceShape },
                        effects = {
                            vibrancy()
                            blur(glass.radius.dp.toPx())
                            if (glass.refraction) {
                                lens(
                                    refractionHeight = 6.dp.toPx(),
                                    refractionAmount = 16.dp.toPx(),
                                    depthEffect = false,
                                )
                            }
                        },
                        highlight = { com.kyant.backdrop.highlight.Highlight.Default },
                        shadow = { com.kyant.backdrop.shadow.Shadow.Default },
                        onDrawSurface = {
                            drawRect(
                                if (glass.dark) Color.Black.copy(alpha = 0.38f)
                                else Color.White.copy(alpha = 0.5f),
                            )
                        },
                    )
                } else {
                    // 关闭模糊或系统低于 API 31 时，原先这里没有任何背景，
                    // 歌名/歌手会直接压在滚动内容上难以阅读；补一层近不透明表面 + 细描边。
                    Modifier
                        .clip(pieceShape)
                        .background(
                            if (dark) Color(0xFF1C1C1E).copy(alpha = 0.94f)
                            else Color.White.copy(alpha = 0.94f),
                        )
                        .border(
                            1.dp,
                            if (dark) Color.White.copy(alpha = 0.12f)
                            else Color.Black.copy(alpha = 0.06f),
                            pieceShape,
                        )
                },
            ),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 6.dp)
                    // 按压缩放只作用于内容 Row（transform，不触发布局），不套到外层 backdrop 节点；
                    // 放在 clickable 之前，缩放封面+文字整体。减弱动效时 barScale 恒为 1f。
                    .graphicsLayer { scaleX = barScale; scaleY = barScale }
                    .clickable(
                        interactionSource = barInteraction,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = "进入播放页",
                        onClick = {
                            if (navigator.backStack.last() != Screen.Player) navigator.push(Screen.Player)
                        },
                    ),
            ) {
                // 切歌时封面淡入 + 轻微缩放，与播放页封面的入场语言一致：
                // 播放页已有 260ms 淡入，但迷你条此前是直接硬切封面；缓存命中时尤其突兀。
                // 以歌曲 id 为 key，同一首歌刷新封面（picUrl -> 兜底大图）不会闪一下。
                // 只动 opacity/scale（transform），不触发布局；减弱动效时初值即为「已显示」，静帧到位。
                key(current.id) {
                    var coverShown by remember { mutableStateOf(reduceMotion) }
                    LaunchedEffect(Unit) { coverShown = true }
                    val coverEnter by animateFloatAsState(
                        targetValue = if (coverShown) 1f else 0f,
                        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 220),
                        label = "miniCoverEnter",
                    )
                    TrackCover(
                        url = state.coverUrl ?: current.meta.picUrl,
                        size = 44.dp,
                        corner = 10.dp,
                        modifier = Modifier.graphicsLayer {
                            alpha = coverEnter
                            scaleX = 0.92f + 0.08f * coverEnter
                            scaleY = 0.92f + 0.08f * coverEnter
                        },
                    )
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        current.name,
                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        current.singer,
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // 触控热区统一到 48dp（Material 无障碍最小Target），图标视觉尺寸不变；
                // 同时给出与播放页一致的按压缩放反馈
                MiniBarAction(
                    icon = if (state.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                    description = if (state.isPlaying) "暂停" else "播放",
                    iconSize = 24.dp,
                    onClick = { LxApp.app.playerRepository.togglePlay() },
                )
                Spacer(Modifier.size(2.dp))
                MiniBarAction(
                    icon = MiuixIcons.Layers,
                    description = "播放队列",
                    iconSize = 22.dp,
                    onClick = {
                        if (navigator.backStack.last() != Screen.PlayQueue) navigator.push(Screen.PlayQueue)
                    },
                )
            }
            // 细进度线：给迷你条一个不占位的播放进度反馈。
            // positionMs 每 500ms 才更新一次，直接赋值会「跳格」；用等速补间把两次更新连起来。
            // 这是全站最后一处未降级的 tween：系统要求减弱动效时瞬时到位（进度跳格是预期的静帧），
            // 与封面淡入、歌词高亮、按压缩放的静帧降级约定保持一致。
            // reduceMotion 复用函数顶部声明（整条与进度线共用同一开关，避免重复读取）
            val rawProgress = if (state.durationMs > 0) {
                (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
            } else {
                0f
            }
            val progress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = tween(durationMillis = 500.motionMs(reduceMotion), easing = LinearEasing),
                label = "miniProgress",
            )
            // 对齐原版 common.allowProgressBarSeek：允许时迷你条进度线可水平拖动跳转。
            // 拖动期间用 -1 之外的本地进度覆盖显示，松手才真正 seek，避免拖动中每帧触发跳转。
            val allowSeek by LxApp.app.settings.allowProgressBarSeek.collectAsState(initial = true)
            var dragProgress by remember { mutableFloatStateOf(-1f) }
            var seekBarWidthPx by remember { mutableFloatStateOf(0f) }
            val dragging = dragProgress >= 0f
            val shownProgress = if (dragging) dragProgress else progress
            // 滑块用 transform 从小到大回弹到位（只缩放、不改布局）；关动画时直接满尺寸出现。
            val thumbScale by animateFloatAsState(
                targetValue = if (dragging && !reduceMotion) 1f else 0.6f,
                animationSpec = Motion.press(),
                label = "miniSeekThumb",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    // 视觉仍是贴底 2dp 细线；外层命中容器向下顶到底、向上扩到 16dp，
                    // 让手指有足够区域按下开始拖动（否则 2dp 几乎点不中）。
                    .height(16.dp)
                    // 可拖动时挂水平拖动手势；关闭开关或无效时长时退化为纯展示。
                    .then(
                        if (allowSeek && state.durationMs > 0) {
                            Modifier.pointerInput(state.durationMs) {
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        if (seekBarWidthPx > 0f) {
                                            dragProgress = (offset.x / seekBarWidthPx).coerceIn(0f, 1f)
                                        }
                                    },
                                    onHorizontalDrag = { change, _ ->
                                        if (seekBarWidthPx > 0f) {
                                            dragProgress = (change.position.x / seekBarWidthPx).coerceIn(0f, 1f)
                                        }
                                    },
                                    onDragEnd = {
                                        if (dragProgress >= 0f) {
                                            LxApp.app.playerRepository.seekTo((dragProgress * state.durationMs).toLong())
                                            dragProgress = -1f
                                        }
                                    },
                                    onDragCancel = { dragProgress = -1f },
                                )
                            }
                        } else Modifier,
                    )
                    .onSizeChanged { seekBarWidthPx = it.width.toFloat() }
                    // 读屏用户也能获知播放进度，而不只是一个装饰性细线
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(shownProgress, 0f..1f)
                        contentDescription = "播放进度"
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                // 真正的 2dp 视觉线，贴容器底部，与改动前的位置/尺寸一致。
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(Dimens.Hairline))
                        .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(shownProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(Dimens.Hairline))
                            .background(MiuixTheme.colorScheme.primary),
                    )
                }
                // 拖动时显示滑块，让「正在拖进度」有明确视觉反馈（此前只有细线在动）。
                // 滑块浮在 2dp 轨道之上，不参与测量，松手即消失，不改变原有命中区与 seek 逻辑。
                if (dragging) {
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.primary)
                            .graphicsLayer {
                                // translation 只影响绘制位置：以轨道左端为原点，滑块中心对齐进度点。
                                translationX = shownProgress * seekBarWidthPx - 5.dp.toPx()
                                scaleX = thumbScale
                                scaleY = thumbScale
                            },
                    )
                }
            }
        }
    }
}

/** 迷你播放条上的图标按钮：48dp 热区 + 按下缩放的触感反馈 */
@Composable
private fun MiniBarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 与全站一致：系统要求减弱动效时按压缩放退化为静帧，只保留点击热区与 Crossfade 图标切换。
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.86f else 1f,
        animationSpec = Motion.press(),
        label = "miniBarPress",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                indication = null,
                interactionSource = interaction,
                // 与 BackIconButton / 播放页图标键一致补 Role.Button：
                // 内层 Icon 的 contentDescription（播放/播放队列）读屏会报成「图像」，加 role 后才是「按钮」。
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 与播放页一致：播放/暂停图标淡入淡出，不硬切
        androidx.compose.animation.Crossfade(
            targetState = icon,
            // 减弱动效下图标瞬时切换：Crossfade 是纯装饰性淡入淡出，
            // 与同函数按压缩放、播放页封面/文字入场的静帧降级保持一致。
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else 180),
            label = "miniBarIcon",
        ) { shownIcon ->
            Icon(shownIcon, contentDescription = description, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
private fun GlassBottomBar(glass: GlassContext, navigator: Navigator) {
    Box(
        Modifier
            .padding(horizontal = 12.dp)
            .drawBackdrop(
                backdrop = glass.content,
                shape = { RoundedCornerShape(Dimens.CornerXL) },
                effects = {
                    vibrancy()
                    blur(glass.radius.dp.toPx())
                    // lens 折射：仅 API 33+ 生效，且 shape 必须是圆角形状
                    if (glass.refraction) {
                        lens(
                            refractionHeight = 12.dp.toPx(),
                            refractionAmount = 24.dp.toPx(),
                            depthEffect = true,
                        )
                    }
                },
                onDrawSurface = {
                    drawRect(if (glass.dark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f))
                },
            ),
    ) {
        NavItems(navigator)
    }
}

/**
 * 无模糊底栏（关闭模糊 / API < 31 降级）。
 *
 * 此前直接返回 [NavItems]，背景由 Scaffold 决定：浅色下是白底压在白列表上，
 * 滚动内容与底栏之间没有任何边界。这里补一层不透明表面 + 1dp 顶部分隔线，
 * 与 [GlassTopBar] 的「底部 1px 分隔」呼应，保证降级路径也有层次。
 */
@Composable
private fun PlainBottomBar(navigator: Navigator) {
    val dark = isInDarkTheme()
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val lineH = Dimens.Hairline.toPx()
                drawRect(
                    color = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(size.width, lineH),
                )
            }
            .background(if (dark) Color(0xFF141414) else Color(0xFFF7F7F7)),
    ) {
        NavItems(navigator)
    }
}

@Composable
private fun NavItems(navigator: Navigator) {
    val current = navigator.backStack.last()
    val items = listOf(
        Triple(Screen.Home, MiuixIcons.Home, "首页"),
        Triple(Screen.Search(), MiuixIcons.Search, "搜索"),
        Triple(Screen.Favorite, MiuixIcons.Favorites, "我喜欢"),
        Triple(Screen.Source, MiuixIcons.Layers, "音源"),
        Triple(Screen.Setting, MiuixIcons.Settings, "设置"),
    )
    NavigationBar {
        // 二级页面没有自己的 tab：映射到所属 tab，避免底栏 5 个全不高亮
        val currentTab = when (current) {
            is Screen.Search -> Screen.Search()
            is Screen.Favorite -> Screen.Favorite
            is Screen.Source -> Screen.Source
            is Screen.Setting -> Screen.Setting
            else -> Screen.Home
        }
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = currentTab == screen,
                onClick = { navigator.setRoot(screen) },
                icon = icon,
                label = label,
            )
        }
    }
}
