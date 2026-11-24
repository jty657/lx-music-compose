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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 统一的「纯文字操作按钮」：把散落各页的裸 `.clickable { Text(...) }` 收敛到一处。
 *
 * 此前搜索页「清空」历史、设置页「删除」不喜欢规则、播放页错误态「下一首」三处
 * 都是系统方角涟漪 + 按下无缩放，且读屏只当作普通文本，与全站
 * [BackIconButton] / [ClearQueueButton] / [RetryHint] 的「indication=null + 按压缩放 +
 * Role.Button + reduceMotion 静帧」语言不一致。这里补齐同一手感：
 *
 * - 命中区用 [minimumInteractiveComponentSize] 兜底到 Material 无障碍最小目标 48dp，
 *   视觉仍是紧凑文字（horizontal 8dp / vertical 14dp 内边距与改动前一致，外观零变化）；
 * - 按压缩放 0.9（[Motion.press]），只动 transform 不触发布局；
 * - `Role.Button` 语义，读屏可识别为按钮；
 * - 系统要求减弱动效时缩放退化为静帧。
 *
 * 颜色由调用方传入（primary / error 等），字号固定为全站一致的 body2。
 */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    /** 文字字号；默认 body2，搜索联想词用 body1 与列表主文本层级一致 */
    fontSize: androidx.compose.ui.unit.TextUnit = MiuixTheme.textStyles.body2.fontSize,
    /** 文本水平对齐；联想下拉需要左对齐，历史「清空」等保持默认居中 */
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    /**
     * 最大行数，默认不限制（保持既有调用方行为不变）。搜索联想词传 1 截断超长词，
     * 对齐原版 TipList 的 `numberOfLines={1}`：避免长词换行撑高下拉、挤动下方结果列表。
     */
    maxLines: Int = Int.MAX_VALUE,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.9f else 1f,
        animationSpec = Motion.press(),
        label = "textActionPress",
    )
    Text(
        text,
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = modifier
            // 命中区前置兜底 48dp，避免被圆角裁剪；缩放只作用于文字本身
            .minimumInteractiveComponentSize()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(Dimens.CornerS))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 14.dp),
    )
}
