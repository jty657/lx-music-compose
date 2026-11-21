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
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
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
import com.lingxu.music.ui.theme.isInDarkTheme
import com.lingxu.music.ui.theme.motionMs
import com.lingxu.music.ui.theme.rememberReduceMotion
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
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
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
    val pieceShape = RoundedCornerShape(20.dp)
    val dark = glass?.dark ?: isInDarkTheme()

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
                    .clickable {
                        if (navigator.backStack.last() != Screen.Player) navigator.push(Screen.Player)
                    },
            ) {
                TrackCover(url = state.coverUrl ?: current.meta.picUrl, size = 44.dp, corner = 10.dp)
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
            val rawProgress = if (state.durationMs > 0) {
                (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
            } else {
                0f
            }
            val progress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                label = "miniProgress",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 2.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f))
                    // 读屏用户也能获知播放进度，而不只是一个装饰性细线
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                        contentDescription = "播放进度"
                    },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(MiuixTheme.colorScheme.primary),
                )
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
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
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
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 与播放页一致：播放/暂停图标淡入淡出，不硬切
        androidx.compose.animation.Crossfade(
            targetState = icon,
            animationSpec = tween(durationMillis = 180),
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
                shape = { RoundedCornerShape(28.dp) },
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
