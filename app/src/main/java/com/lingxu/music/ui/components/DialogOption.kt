package com.lingxu.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion

/**
 * 弹窗内「铺满整行的纯文字选项」的统一点击修饰。
 *
 * 此前这些选项（我的列表排序/操作弹窗、设置页定时弹窗）是全站最后一处裸 [clickable]：
 * 系统默认 indication 会在整行矩形热区里画出方角涟漪，与圆角玻璃弹窗格格不入，
 * 按下也没有任何缩放反馈；读屏只当作普通文本，无法识别为可操作项。
 *
 * 这里与 [BackIconButton]、[RetryHint]、[TextActionButton] 以及各页的
 * `deletePressClickable` / `queueActionClickable` 统一为同一语言：
 * - `indication = null` 去掉方角涟漪，改为轻微按压缩放（只动 transform，不触发布局）；
 * - `Role.Button` 语义，读屏可识别为按钮；
 * - 系统要求减弱动效时缩放退化为静帧。
 *
 * 幅度取 0.97（而非紧凑图标键的 0.9）：整行文字面积大，1.5~3% 的轻触更接近
 * MIUI / iOS 列表项手感，避免整行明显「塌陷」。字号、颜色、padding、热区全部由调用点
 * 原有 Modifier 决定，本修饰不改变任何静态视觉。
 */
@Composable
fun Modifier.dialogOptionClickable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.97f else 1f,
        animationSpec = Motion.press(),
        label = "dialogOptionPress",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
}
