package com.lingxu.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 统一的「加载失败，点击重试」提示：error 色文字 + 48dp 最小点击热区 + 圆角裁剪按压反馈。
 *
 * 此前多个页面各写一遍 `Text + clickable + padding(vertical=8.dp)`，热区偏小且样式不统一。
 * 命中区用 [minimumInteractiveComponentSize] 兜底到 Material 无障碍最小目标 48dp（视觉尺寸不变），
 * 与 [FilterChip]、[BackIconButton] 的可点元素保持一致。
 * [text] 可传自定义文案（部分页面带具体错误原因）。
 */
@Composable
fun RetryHint(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        color = MiuixTheme.colorScheme.error,
        fontSize = MiuixTheme.textStyles.body2.fontSize,
        modifier = modifier
            // 命中区兜底 48dp，视觉仍是紧凑的 error 文字；放在 clip 之前避免被圆角裁掉
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
    )
}
