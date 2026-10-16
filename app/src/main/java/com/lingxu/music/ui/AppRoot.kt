package com.lingxu.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.lingxu.music.LxApp
import com.lingxu.music.ui.components.GlassContext
import com.lingxu.music.ui.components.GlassHost
import com.lingxu.music.ui.components.LocalGlass
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
import com.lingxu.music.ui.theme.LxTheme
import com.lingxu.music.ui.theme.isInDarkTheme
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
        val autoHidePlayBar by LxApp.app.settings.autoHidePlayBar.collectAsState(initial = true)
        // 列表向下滚动时隐藏迷你播放条（对应原版 common.autoHidePlayBar）
        var miniBarVisible by remember { mutableStateOf(true) }
        val miniBarScroll = remember { mutableStateOf(0) }
        // 关闭模糊或系统不支持 RenderEffect（API < 31）时，radius 传 0 让玻璃面变成纯 tint
        val effectiveRadius = if (blurEnabled) blurRadius else 0f
        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalMiniBarScroll provides miniBarScroll,
        ) {
            GlassHost(
                dark = dark,
                radius = effectiveRadius,
                refraction = runtimeShaderSupported && blurEnabled,
            ) {
                Box(Modifier.fillMaxSize()) {
                    // 主内容（被玻璃模糊的目标）
                    MainScaffold(
                        navigator = navigator,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // 底部：迷你播放条 + 导航栏
                    LaunchedEffect(miniBarScroll.value) {
                        miniBarVisible = autoHidePlayBar && miniBarScroll.value <= 0
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding(),
                    ) {
                        if (miniBarVisible) MiniPlayerBar(navigator)
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

@Composable
private fun MainScaffold(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        val current = navigator.backStack.last()
        val playState by LxApp.app.playerRepository.state.collectAsState()
        // 系统返回键：非根页时回退一级
        BackHandler(enabled = navigator.backStack.size > 1) {
            navigator.pop()
        }
        // 底部留白：导航栏 + （有歌曲时的）迷你播放条
        val bottomInset = if (playState.current != null) 132.dp else 76.dp
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomInset),
        ) {
            when (current) {
                is Screen.Home -> HomeScreen()
                is Screen.Search -> SearchScreen(initialKeyword = current.keyword)
                is Screen.Source -> SourceScreen()
                is Screen.Setting -> SettingScreen()
                is Screen.Player -> PlayerScreen()
                is Screen.Favorite -> FavoriteScreen()
                is Screen.BoardList -> BoardDetailScreen(
                    source = current.source,
                    bangId = current.bangId,
                    name = current.name,
                )
                is Screen.SongList -> SongListScreen(source = current.source)
                is Screen.SongListDetail -> com.lingxu.music.ui.screens.songlist.SongListDetailScreen(
                    source = current.source,
                    id = current.id,
                    name = current.name,
                )
                is Screen.MyLists -> MyListsScreen()
                is Screen.PlayQueue -> com.lingxu.music.ui.screens.playqueue.PlayQueueScreen()
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

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp)
            .then(
                if (glass != null && glass.radius > 0f) {
                    Modifier.drawBackdrop(
                        backdrop = glass.content,
                        shape = { RoundedCornerShape(20.dp) },
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
                    Modifier
                },
            ),
    ) {
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
        Icon(
            if (state.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
            contentDescription = if (state.isPlaying) "暂停" else "播放",
            modifier = Modifier
                .size(36.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = { LxApp.app.playerRepository.togglePlay() },
                ),
        )
        Spacer(Modifier.size(8.dp))
        Icon(
            MiuixIcons.Layers,
            contentDescription = "播放队列",
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = {
                        if (navigator.backStack.last() != Screen.PlayQueue) navigator.push(Screen.PlayQueue)
                    },
                ),
        )
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

@Composable
private fun PlainBottomBar(navigator: Navigator) {
    NavItems(navigator)
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
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = current == screen,
                onClick = {
                    navigator.backStack.clear()
                    navigator.backStack.add(screen)
                },
                icon = icon,
                label = label,
            )
        }
    }
}
