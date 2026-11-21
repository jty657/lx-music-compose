package com.lingxu.music.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.lingxu.music.ui.navigation.LocalMiniBarScroll

/**
 * 列表滚动时上报方向，供迷你播放条在向下滚动时隐藏、回顶部时恢复
 * （对应原版 common.autoHidePlayBar）。
 *
 * 用法：在 LazyColumn 同级调用，传入同一个 LazyListState。
 */
@Composable
fun ReportScrollForMiniBar(listState: LazyListState) {
    val reporter = LocalMiniBarScroll.current
    val lastIndex = remember { mutableIntStateOf(0) }
    val lastOffset = remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        // 同时观察下标与行内偏移量：只比下标时，长列表里「点条目前挪半屏」「下拉回弹」
        // 这类不动下标的滚动手势不会被识别，迷你播放条要么该藏不藏、要么乱闪。
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                reporter.value = when {
                    // 回到顶部：恢复显示
                    index == 0 && offset == 0 -> 0
                    // 向下滚动（下标前进或同下标偏移变大）→ 隐藏
                    index > lastIndex.intValue ||
                        (index == lastIndex.intValue && offset > lastOffset.intValue + ScrollSlopPx) -> 1
                    // 向上滚动 → 恢复
                    index < lastIndex.intValue ||
                        (index == lastIndex.intValue && offset < lastOffset.intValue - ScrollSlopPx) -> 0
                    // 微小抖动不改变状态，避免播放条边缘反复抖动
                    else -> reporter.value
                }
                lastIndex.intValue = index
                lastOffset.intValue = offset
            }
    }
}

/** 判定「真实滚动」的最小像素差，过滤列表吸附/弹性回弹造成的 1–2px 抖动 */
private const val ScrollSlopPx = 8
