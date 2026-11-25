package com.lingxu.music.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

/**
 * 列表滚动位置记忆（对齐原版 `list.isSaveScrollLocation`，默认开启）。
 *
 * 原版进入二级页（歌单详情、榜单详情、搜索结果等）再返回时，会保留离开时的滚动位置；
 * 移植版此前每次返回都从顶部开始，长列表里要重新滑回原处，体验割裂。
 *
 * 设计取舍：
 * - 本项目用自研 Navigator + AnimatedContent 切页，页面离开时 composable 被销毁，
 *   `rememberSaveable` 的状态也随之丢弃，无法覆盖「返回再进入」这个主场景。
 *   因此用进程内存里的单例 Map 按 [key] 保存两个 Int（首个可见项下标 + 行内偏移），
 *   活到进程结束——既覆盖主场景，也与原版（位置存于内存状态）行为一致。
 * - 位置按 [key] 隔离：不同歌单/榜单各有自己的位置，避免 A 歌单的位置串到 B。
 * - 只存两个 Int，不存整个 [LazyListState]（后者持有列表数据引用，长期持有会泄漏）。
 * - 异步加载的详情页首帧列表为空，[rememberLazyListState] 会把初始下标钳到 0；
 *   这里在 [LaunchedEffect] 里等列表条数就绪后再 [LazyListState.scrollToItem] 补一次，
 *   确保「数据到了之后」也能恢复到离开前的位置。
 * - 纯 UI 层组件：不碰播放核心、路由与状态管理，不新增依赖。
 *
 * @param key 稳定标识，通常用页面参数拼成，如 "songListDetail:kw:xxx"；传 null 表示不记忆
 * @return 已恢复初始位置的 [LazyListState]，直接传给 LazyColumn(state = ...)
 */
@Composable
fun rememberSavedLazyListState(key: String?): LazyListState {
    // 读取时只取一次：key 变化（进入不同详情页）时状态对象随之重建，
    // 初值来自 store 里该 key 的上次离开位置；同一 key 反复进入能连续累计。
    val saved = remember(key) { key?.let { ScrollPositionStore.get(it) } }
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = saved?.first ?: 0,
        initialFirstVisibleItemScrollOffset = saved?.second ?: 0,
    )

    // 数据异步到达的兜底：首帧列表为空时初始下标被钳到 0，这里等条数超过保存位置后补滚一次。
    // snapshotFlow 只在条数变化时发射，first{} 命中即结束，不常驻逐帧调度；切走页面时随组合取消。
    LaunchedEffect(key, saved) {
        val target = saved?.first ?: return@LaunchedEffect
        if (target <= 0) return@LaunchedEffect
        snapshotFlow { state.layoutInfo.totalItemsCount }
            .first { count -> count > target }
        state.scrollToItem(target, saved?.second ?: 0)
    }

    // 离开页面时写回当前位置。DisposableEffect 随 composable 销毁触发，
    // 正好对应 AnimatedContent 切走页面的时刻。
    DisposableEffect(key) {
        onDispose {
            if (key != null) {
                ScrollPositionStore.put(
                    key,
                    state.firstVisibleItemIndex,
                    state.firstVisibleItemScrollOffset,
                )
            }
        }
    }
    return state
}

/** 进程内滚动位置缓存：key -> (首个可见项下标, 行内偏移) */
private object ScrollPositionStore {
    private val positions = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, Int>>()

    fun get(key: String): Pair<Int, Int>? = positions[key]

    fun put(key: String, index: Int, offset: Int) {
        positions[key] = index to offset
    }
}
