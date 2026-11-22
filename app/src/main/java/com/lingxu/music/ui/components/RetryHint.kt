package com.lingxu.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 统一的「加载失败，点击重试」提示：error 色文字 + 48dp 最小点击热区 + 圆角裁剪按压反馈。
 *
 * 此前多个页面各写一遍 `Text + clickable + padding(vertical=8.dp)`，热区偏小且样式不统一。
 * 命中区用 [minimumInteractiveComponentSize] 兜底到 Material 无障碍最小目标 48dp（视觉尺寸不变），
 * 与 [FilterChip]、[BackIconButton] 的可点元素保持一致。
 * [text] 可传自定义文案（部分页面带具体错误原因）。
 *
 * 交互语言与全站对齐（本轮）：此前这里是全站共用组件里唯一残留的裸 [clickable]（系统方角涟漪、
 * 按下无缩放），而 [BackIconButton] / `ClearQueueButton` / `queueActionClickable` 等所有文字/
 * 图标键均已统一为「`indication=null` + 按压缩放 + `reduceMotion` 静帧降级」。这里补齐同一手感：
 * indication=null + 轻微缩放（[Motion.press]）+ `Role.Button` 语义；error 色、字号、48dp 热区、
 * clip 与 padding 全部保留，减弱动效时退化为静帧。
 */
@Composable
fun RetryHint(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.9f else 1f,
        animationSpec = Motion.press(),
        label = "retryHintPress",
    )
    Text(
        text,
        color = MiuixTheme.colorScheme.error,
        fontSize = MiuixTheme.textStyles.body2.fontSize,
        modifier = modifier
            // 命中区兜底 48dp，视觉仍是紧凑的 error 文字；放在 clip 之前避免被圆角裁掉
            .minimumInteractiveComponentSize()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(Dimens.CornerS))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 12.dp),
    )
}
