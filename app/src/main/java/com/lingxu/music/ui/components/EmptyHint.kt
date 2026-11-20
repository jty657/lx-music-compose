package com.lingxu.music.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
 */
@Composable
fun EmptyHint(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
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
