package com.lxmusic.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** 页面路由 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Source : Screen("source")
    object Setting : Screen("setting")
    object Player : Screen("player")
    object BoardList : Screen("boardList")
    object Favorite : Screen("favorite")
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator found") }

/** 简单导航器（Navigation3 的 backStack 驱动） */
class Navigator(
    val backStack: MutableList<Screen>,
) {
    fun push(screen: Screen) {
        backStack.add(screen)
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun replace(screen: Screen) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = screen
        } else {
            backStack.add(screen)
        }
    }
}

@Composable
fun rememberNavigator(): Navigator {
    val backStack = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<Screen>(Screen.Home)
    }
    return androidx.compose.runtime.remember { Navigator(backStack) }
}
