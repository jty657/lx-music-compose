package com.lingxu.music.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.MotionFastMs
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 未选中 chip 的描边色（浅色黑 7%，深色白 12%） */
internal fun chipBorderColor(dark: Boolean): Color =
    if (dark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)

/**
 * 可选项胶囊（筛选 chip）。
 *
 * 搜索页「音源」「音乐/歌单」以及歌单广场的排序此前各写一套：[GlassSurface] + tint +
 * 两套不同内边距（14/8dp 与 16/9dp），圆角跟着卡片走成 20dp —— 视觉上像「按钮」，
 * 而卡片（20dp 圆角）又要表达「内容块」，两者撞形。这里固化：
 *
 * - 胶囊形（[Dimens.ChipCorner] 全圆角），与卡片区分开；
 * - 选中 = 主色淡底 + 主色文字 + 主色描边；未选中 = 中性文字 + 淡描边；底色 160ms 渐变；
 * - [MinimumInteractiveComponentSize] 保证 48dp 命中高度（视觉仍是紧凑胶囊）；
 * - 语义 `selected` + `Role.Tab`：读屏能播报「已选中」。
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalPadding: Int = 14,
) {
    val dark = LocalGlass.current?.dark ?: false
    val reduceMotion = LocalReduceMotion.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val bgTarget = when {
        !enabled -> Color.Transparent
        // 选中：主色 15%，按下再加深到 24%（方向一致：越按越"实"）
        selected -> MiuixTheme.colorScheme.primary.copy(alpha = if (pressed) 0.24f else 0.15f)
        pressed -> MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val bg by animateColorAsState(
        targetValue = bgTarget,
        animationSpec = tween(if (reduceMotion) 0 else MotionFastMs),
        label = "chipBg",
    )
    val border by animateColorAsState(
        targetValue = when {
            selected -> MiuixTheme.colorScheme.primary.copy(alpha = 0.55f)
            enabled -> chipBorderColor(dark)
            else -> Color.Transparent
        },
        animationSpec = tween(if (reduceMotion) 0 else MotionFastMs),
        label = "chipBorder",
    )
    val textColor = when {
        !enabled -> MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.45f)
        selected -> MiuixTheme.colorScheme.primary
        else -> MiuixTheme.colorScheme.onSurface
    }
    val shape = RoundedCornerShape(Dimens.ChipCorner)

    Row(
            modifier
                // 命中区 ≥48dp：视觉仍是紧凑胶囊，但可点区域足够大。
                // 注意要放在 clip 之前 —— 放之后会被圆角裁掉溢出部分。
                .minimumInteractiveComponentSize()
                .clip(shape)
                .background(bg, shape)
                .border(Dimens.Hairline, border, shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                )
                .semantics {
                    this.selected = selected
                    role = Role.Tab
                }
                // 纵向 9dp + body2 文本高度 ≈ 36dp 视觉高度；48dp 命中区由 MinimumInteractiveComponentSize 兜底
                .padding(horizontal = horizontalPadding.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            color = textColor,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
        )
    }
}
