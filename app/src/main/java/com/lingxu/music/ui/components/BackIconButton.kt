package com.lingxu.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

/**
 * 统一的返回按钮：图标视觉尺寸保持 28dp，点击热区撑到 48dp（Material 无障碍最小目标）。
 *
 * 此前多个二级页各自用 `Icon(size=28dp)` 直接挂 clickable，热区仅 28dp，手指点不准；
 * 抽成公共组件后统一热区、统一语义（contentDescription = "返回"），也消除重复代码。
 */
@Composable
fun BackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(MiuixIcons.Back, contentDescription = "返回", modifier = Modifier.size(28.dp))
    }
}
