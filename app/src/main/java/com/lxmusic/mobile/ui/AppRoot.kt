package com.lxmusic.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Boximport androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import com.lxmusic.mobile.ui.navigation.Navigator
import com.lxmusic.mobile.ui.navigation.Screen
import com.lxmusic.mobile.ui.navigation.rememberNavigator
import com.lxmusic.mobile.ui.screens.favorite.FavoriteScreen
import com.lxmusic.mobile.ui.screens.board.BoardDetailScreen
import com.lxmusic.mobile.ui.screens.home.HomeScreen
import com.lxmusic.mobile.ui.screens.player.PlayerScreen
import com.lxmusic.mobile.ui.screens.search.SearchScreen
import com.lxmusic.mobile.ui.screens.setting.SettingScreen
import com.lxmusic.mobile.ui.screens.source.SourceScreen
import com.lxmusic.mobile.ui.theme.LxTheme
import com.lxmusic.mobile.ui.theme.isInDarkTheme
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
    val blurEnabled by LxApp.app.settings.blurEnabled.collectAsState(initial = true)
    val blurRadius by LxApp.app.settings.blurRadius.collectAsState(initial = 24f)

    LxTheme(themeMode = themeMode) {
        val navigator = rememberNavigator()
        val dark = isInDarkTheme()
        CompositionLocalProvider(LocalNavigator provides navigator) {
            Box(Modifier.fillMaxSize()) {
                val backdrop = rememberLayerBackdrop {
                    drawRect(if (dark) Color.Black else Color.White)
                    drawContent()
                }

                // 主内容（被玻璃模糊的目标）
                MainScaffold(
                    navigator = navigator,
                    modifier = Modifier.layerBackdrop(backdrop),
                )

                // 底部：迷你播放条 + 导航栏
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                ) {
                    MiniPlayerBar(navigator)
                    if (blurEnabled) {
                        GlassBottomBar(backdrop, blurRadius, dark, navigator)
                    } else {
                        PlainBottomBar(navigator)
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
                is Screen.Search -> SearchScreen()
                is Screen.Source -> SourceScreen()
                is Screen.Setting -> SettingScreen()
                is Screen.Player -> PlayerScreen()
                is Screen.Favorite -> FavoriteScreen()
                is Screen.BoardList -> BoardDetailScreen(
                    source = current.source,
                    bangId = current.bangId,
                    name = current.name,
                )
            }
        }
    }
}

/** 迷你播放条：显示当前歌曲并可进入播放页 */
@Composable
private fun MiniPlayerBar(navigator: Navigator) {
    val state by LxApp.app.playerRepository.state.collectAsState()
    val current = state.current ?: return

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
    }
}

@Composable
private fun GlassBottomBar(
    backdrop: LayerBackdrop,
    blurRadius: Float,
    dark: Boolean,
    navigator: Navigator,
) {
    Box(
        Modifier
            .padding(horizontal = 12.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = {
                    vibrancy()
                    blur(blurRadius.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(if (dark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f))
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
        Triple(Screen.Search, MiuixIcons.Search, "搜索"),
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
