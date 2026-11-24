package com.lingxu.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.MotionBaseMs
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 统一的空列表提示：居中 + 40dp 占位图标 + 次要色文案。
 *
 * 此前收藏页、播放队列、歌单详情、我的列表、搜索页、榜单详情等各写一遍
 * `Column(center) → Icon → Spacer → Text`，留白（48/56/64dp）与色调逐渐漂移。
 * 抽成公共组件后统一视觉语言与维护入口。图标由调用方传入，组件本身不引用
 * 任何具体图标常量，避免 import 规则耦合。
 *
 * 入场给一次很短的上浮淡入（[MotionBaseMs]，位移 6dp，只动 transform/alpha，不触发布局），
 * 让「加载完成 → 空」不是硬切；减弱动效时直接静止呈现。
 */
@Composable
fun EmptyHint(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = LocalReduceMotion.current
    // 减弱动效时初值即为「已显示」，避免一次多余的入场动画
    var shown by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(if (reduceMotion) 0 else MotionBaseMs),
        label = "emptyAlpha",
    )
    val offsetDp by animateFloatAsState(
        targetValue = if (shown) 0f else 6f,
        animationSpec = tween(if (reduceMotion) 0 else MotionBaseMs),
        label = "emptyOffset",
    )
    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                // dp → px：graphicsLayer 的 translationY 是像素
                translationY = offsetDp * density
            }
            .padding(vertical = Dimens.EmptyStateVPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            // 装饰性图标：文案已由下方 Text 承载，读屏不应再播报一次「图标」。
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
        )
    }
}
