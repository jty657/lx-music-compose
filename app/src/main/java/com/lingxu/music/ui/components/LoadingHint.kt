package com.lingxu.music.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val LoadingDotSize = 6.dp

/**
 * 统一的「加载中」提示：居中 + 三点流水灯 + 次要色文案。
 *
 * 此前歌单详情、榜单详情的首次加载是一行静态文本「正在加载…」，慢网络上与
 * 「无数据」几乎无法区分，观感像卡住；而全站其它加载态（首页骨架、加载更多
 * Crossfade、PlayingIndicator 律动）都已有反馈。这里补齐同一语言：
 *
 * - 三个主色小点用不同周期的 opacity 呼吸自然错开相位，表达「进行中」；
 *   与 [PlayingIndicator] 一致，只用 alpha（transform/opacity 家族），不触发布局；
 * - 系统「移除动画」（[LocalReduceMotion]）时退化为三点等亮静帧，不启动无限动画，
 *   避免持续逐帧调度，与前庭无障碍和低端机省电约定一致；
 * - 文案与留白对齐 [EmptyHint]（vertical 56dp / body2 / 次要色），两者互为
 *   「加载中 → 空」的前后状态，视觉连贯；
 * - [text] 由调用方传入具体文案，组件不耦合任何页面语义。
 */
@Composable
fun LoadingHint(
    text: String,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = LocalReduceMotion.current
    // 语义由内层 Text 承载即可：此前这里也给整个 Column 设了相同的 contentDescription，
    // 读屏会把 Container 和 Text 的同一句文案连读两遍。去掉外层语义，避免重复播报。
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.EmptyStateVPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadingDot(index = 0, reduceMotion = reduceMotion)
            LoadingDot(index = 1, reduceMotion = reduceMotion)
            LoadingDot(index = 2, reduceMotion = reduceMotion)
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

/** 单个呼吸圆点：减弱动效时用静态 alpha，不启动无限动画 */
@Composable
private fun LoadingDot(index: Int, reduceMotion: Boolean) {
    if (reduceMotion) {
        DotBox(alpha = 0.6f)
    } else {
        val transition = rememberInfiniteTransition(label = "loadingDots")
        // 三个不同周期（560/760/660ms）自然错开相位，形成依次起伏的流水灯，
        // 而不是三点一起亮灭；Reverse 让呼吸往复，幅度 0.35→1 克制不刺眼。
        val period = when (index) {
            0 -> 560
            1 -> 760
            else -> 660
        }
        val animated by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = period, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "loadingDot$index",
        )
        DotBox(alpha = animated)
    }
}

/** 圆点本体：固定尺寸，起伏只改 graphicsLayer 的 alpha（不触发布局） */
@Composable
private fun DotBox(alpha: Float) {
    Box(
        Modifier
            .size(LoadingDotSize)
            .clip(CircleShape)
            .graphicsLayer { this.alpha = alpha }
            .background(MiuixTheme.colorScheme.primary),
    )
}
