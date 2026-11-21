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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.LocalReduceMotion
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val BarWidth = 3.dp
private val BarHeight = 15.dp

/**
 * 「正在播放」的三条律动音柱（对应 LX Music 手机版播放列表中的播放态标识）。
 *
 * 之前队列里当前曲目只是把 Play 图标染成主色，与「可播放但未播放」的行差异很弱；
 * 这里用三条不同周期的音柱表达「正在出声」，暂停时停在半高静帧而不是继续空转，
 * 避免暂停后仍每帧调度动画。
 *
 * 仅应用于当前曲目（列表中最多一行），且 Compose 动画会遵循系统「动画时长缩放」，
 * 关闭动画（时长缩放为 0）时自动变成静帧，不影响低性能设备。
 */
@Composable
fun PlayingIndicator(
    playing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier.semantics {
            contentDescription = if (playing) "正在播放" else "已暂停"
        },
        contentAlignment = Alignment.Center,
    ) {
        val reduceMotion = LocalReduceMotion.current
        if (playing && !reduceMotion) {
            val transition = rememberInfiniteTransition(label = "playingBars")
            // 三条不同时长：相位差让律动看起来更自然，而不是整排一起上下
            val first by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "barFirst",
            )
            val second by transition.animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 620, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "barSecond",
            )
            val third by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 540, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "barThird",
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Bar(first, color)
                Bar(second, color)
                Bar(third, color)
            }
        } else {
            // 暂停态；或系统要求减弱动效时——用一帧静态波形（高度错落）表达「正在出声」，
            // 保留辨识度，但不需要每帧调度动画。
            val heights = if (playing) listOf(0.62f, 0.95f, 0.78f) else listOf(0.45f, 0.45f, 0.45f)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                heights.forEach { h -> Bar(h, if (playing) color else color.copy(alpha = 0.75f)) }
            }
        }
    }
}

@Composable
private fun Bar(scale: Float, color: Color) {
    Box(
        Modifier
            .width(BarWidth)
            // 固定布局高度，律动只改 transform 的 scaleY：每帧只走绘制层，
            // 不再因 height 变化触发 measure/layout（队列滚动时可避免整行重排）。
            // transformOrigin 设在底边，与外层 Row 的 Bottom 对齐配合，
            // 视觉与原先的 height 缩放一致（从底部向上起伏）。
            .height(BarHeight)
            .graphicsLayer {
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .clip(RoundedCornerShape(1.5.dp))
            .background(color),
    )
}
