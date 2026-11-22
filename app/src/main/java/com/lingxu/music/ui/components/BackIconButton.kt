package com.lingxu.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

/**
 * 统一的返回按钮：图标视觉尺寸保持 28dp，点击热区撑到 48dp（Material 无障碍最小目标）。
 *
 * 此前多个二级页各自用 `Icon(size=28dp)` 直接挂 clickable，热区仅 28dp，手指点不准；
 * 抽成公共组件后统一热区、统一语义（contentDescription = "返回"），也消除重复代码。
 *
 * 按压反馈：去掉涟漪（系统涟漪在 48dp 方形热区里会画成方角，和返回图标不搭），
 * 改为轻微缩放 0.9，与播放页图标键同一手感（[Motion.press]）。
 */
@Composable
fun BackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.9f else 1f,
        animationSpec = Motion.press(),
        label = "backScale",
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                indication = null,
                interactionSource = interaction,
                // 补 Role.Button：内层 Icon 只有 contentDescription，读屏会报成「返回 图像」而非
                // 「返回 按钮」，与全站 TextActionButton/DialogOption 的按钮语义不一致。
                // 仅加语义，点击行为与热区完全不变。
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(MiuixIcons.Back, contentDescription = "返回", modifier = Modifier.size(28.dp))
    }
}
