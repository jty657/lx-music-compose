package com.lingxu.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** 页面路由 */
sealed class Screen(val route: String) {
    object Home : Screen("home")

    /** 搜索页（可携带初始关键词，供热搜/历史直接进入） */
    data class Search(val keyword: String = "") : Screen("search")
    object Source : Screen("source")
    object Setting : Screen("setting")
    object Player : Screen("player")
    object Favorite : Screen("favorite")

    /** 榜单详情页（携带源/榜单 id/名称） */
    data class BoardList(
        val source: String,
        val bangId: String,
        val name: String,
    ) : Screen("boardList")

    /** 歌单列表页（平台歌单广场） */
    data class SongList(
        val source: String,
    ) : Screen("songList")

    /** 歌单详情页（歌单内歌曲） */
    data class SongListDetail(
        val source: String,
        val id: String,
        val name: String,
    ) : Screen("songListDetail")

    /** 我的列表（本地歌单管理） */
    object MyLists : Screen("myLists")

    /** 播放队列 */
    object PlayQueue : Screen("playQueue")
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator found") }

/**
 * 列表滚动状态上报：列表向下滚动时写入正数、回顶部写 0，
 * 供 AppRoot 决定是否隐藏迷你播放条（对应原版 common.autoHidePlayBar）
 */
val LocalMiniBarScroll = staticCompositionLocalOf<androidx.compose.runtime.MutableState<Int>> {
    androidx.compose.runtime.mutableStateOf(0)
}

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
