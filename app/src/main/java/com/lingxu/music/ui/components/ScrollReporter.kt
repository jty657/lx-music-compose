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
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            // 下标变大 = 向下滚动 -> 隐藏；回到顶部 -> 恢复
            reporter.value = when {
                index == 0 -> 0
                index > lastIndex.intValue -> 1
                else -> 0
            }
            lastIndex.intValue = index
        }
    }
}
