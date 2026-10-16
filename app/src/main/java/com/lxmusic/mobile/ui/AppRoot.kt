package com.lxmusic.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import com.lxmusic.mobile.ui.navigation.Navigator
import com.lxmusic.mobile.ui.navigation.Screen
import com.lxmusic.mobile.ui.navigation.rememberNavigator
import com.lxmusic.mobile.ui.screens.home.HomeScreen
import com.lxmusic.mobile.ui.screens.player.PlayerScreen
import com.lxmusic.mobile.ui.screens.search.SearchScreen
import com.lxmusic.mobile.ui.screens.setting.SettingScreen
import com.lxmusic.mobile.ui.screens.source.SourceScreen
import com.lxmusic.mobile.ui.theme.LxTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.drawBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.flow.map
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Search

@Composable
fun AppRoot(viewModel: MainViewModel = viewModel()) {
    val themeMode by LxApp.app.settings.theme.collectAsState(initial = "system")
    val blurEnabled by LxApp.app.settings.blurEnabled.collectAsState(initial = true)
    val blurRadius by LxApp.app.settings.blurRadius.collectAsState(initial = 24f)

    LxTheme(themeMode = themeMode) {
        val navigator = rememberNavigator()
        CompositionLocalProvider(LocalNavigator provides navigator) {
            Box(Modifier.fillMaxSize()) {
                val backdrop = rememberLayerBackdrop {
                    drawRect(Color.White)
                    drawContent()
                }

                // 主内容（被玻璃模糊的目标）
                MainScaffold(
                    navigator = navigator,
                    modifier = Modifier.layerBackdrop(backdrop),
                )

                // 液态玻璃悬浮层：底部导航 + 播放条
                if (blurEnabled) {
                    GlassBottomBar(
                        backdrop = backdrop,
                        blurRadius = blurRadius,
                        navigator = navigator,
                    )
                } else {
                    PlainBottomBar(navigator = navigator)
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 72.dp),
        ) {
            when (current) {
                is Screen.Home -> HomeScreen()
                is Screen.Search -> SearchScreen()
                is Screen.Source -> SourceScreen()
                is Screen.Setting -> SettingScreen()
                is Screen.Player -> PlayerScreen()
                is Screen.BoardList -> HomeScreen()
            }
        }
    }
}

@Composable
private fun GlassBottomBar(
    backdrop: Any,
    blurRadius: Float,
    navigator: Navigator,
) {
    val current = navigator.backStack.last()
    val items = listOf(
        Screen.Home to MiuixIcons.Home,
        Screen.Search to MiuixIcons.Search,
        Screen.Setting to MiuixIcons.Settings,
    )
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = {
                    vibrancy()
                    blur(blurRadius.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.45f)) },
            ),
    ) {
        NavigationBar {
            items.forEach { (screen, icon) ->
                NavigationBarItem(
                    selected = current == screen,
                    onClick = {
                        navigator.backStack.clear()
                        navigator.backStack.add(screen)
                    },
                    icon = icon,
                    label = when (screen) {
                        is Screen.Home -> "首页"
                        is Screen.Search -> "搜索"
                        else -> "设置"
                    },
                )
            }
        }
    }
}

@Composable
private fun PlainBottomBar(navigator: Navigator) {
    val current = navigator.backStack.last()
    val items = listOf(
        Screen.Home to MiuixIcons.Home,
        Screen.Search to MiuixIcons.Search,
        Screen.Setting to MiuixIcons.Settings,
    )
    NavigationBar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding(),
    ) {
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = current == screen,
                onClick = {
                    navigator.backStack.clear()
                    navigator.backStack.add(screen)
                },
                icon = icon,
                label = when (screen) {
                    is Screen.Home -> "首页"
                    is Screen.Search -> "搜索"
                    else -> "设置"
                },
            )
        }
    }
}
