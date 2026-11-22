package com.lingxu.music.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.MotionFastMs
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Text

/**
 * 统一的「加载更多」按钮：列表触底/手动加载下一页时复用。
 *
 * 此前 5 处（搜索音乐/歌单、歌单广场、歌单详情、榜单详情）各写一遍
 * `Button { Text(if (loading) "加载中…" else "加载更多") }`，文案随 loading 硬切，
 * 在慢网络上「点一下→瞬间变字→长时间等待」缺乏过渡，观感像卡住。
 * 这里用 [Crossfade] 让两种文案平滑淡变（[MotionFastMs]），loading 时按钮保持
 * 禁用态（沿用原 `enabled = !loading`，不改变加载触发逻辑）；系统要求减弱动效时
 * 瞬时切换。文案、字号、fillMaxWidth、外边距全部交由调用点保持原样。
 */
@Composable
fun LoadMoreButton(
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = LocalReduceMotion.current
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier,
    ) {
        Crossfade(
            targetState = loading,
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else MotionFastMs),
            label = "loadMoreText",
        ) { isLoading ->
            Text(if (isLoading) "加载中…" else "加载更多")
        }
    }
}
