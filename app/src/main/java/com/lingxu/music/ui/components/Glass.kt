package com.lingxu.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

/**
 * 玻璃效果上下文：由 AppRoot 创建并通过 CompositionLocal 下发，
 * 各页面直接取用，不必自己建 backdrop。
 *
 * [content] 包住页面内容，产出「背后有什么」供玻璃面采样；
 * [radius] 是用户在设置里选的模糊强度（dp）。
 */
class GlassContext(
    val content: LayerBackdrop,
    val dark: Boolean,
    val radius: Float,
    /** API 33+ 才为 true：折射与方向光高光依赖 RuntimeShader */
    val refraction: Boolean,
)

val LocalGlass = androidx.compose.runtime.staticCompositionLocalOf<GlassContext?> { null }

/**
 * 便捷版玻璃卡片：自动从 [LocalGlass] 取 backdrop。
 * 关闭模糊或 API < 31 时退化为普通半透明卡片，观感一致不破版。
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val interaction = remember { MutableInteractionSource() }
    val fallbackTint = tint
        ?: if (glass?.dark != false) Color.Black.copy(alpha = 0.22f)
        else Color.White.copy(alpha = 0.34f)
    val clickable = if (onClick != null && onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interaction,
            indication = null,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    } else if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(clickable)
            .then(
                if (glass != null && glass.radius > 0f) {
                    Modifier.drawBackdrop(
                        backdrop = glass.content,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(glass.radius.dp.toPx())
                            if (glass.refraction) {
                                lens(
                                    refractionHeight = 8.dp.toPx(),
                                    refractionAmount = 16.dp.toPx(),
                                    depthEffect = false,
                                )
                            }
                        },
                        highlight = { Highlight.Default },
                        shadow = { Shadow.Default },
                        onDrawSurface = { drawRect(fallbackTint) },
                    )
                } else {
                    Modifier.clip(shape).background(fallbackTint, shape)
                },
            ),
        content = content,
    )
}

/**
 * 玻璃弹窗：全屏磨砂 scrim + 居中圆角玻璃面板，替代普通 AlertDialog。
 * 关闭模糊时退化为普通半透明面板。
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (glass != null && glass.radius > 0f) {
                        Modifier.drawBackdrop(
                            backdrop = glass.content,
                            shape = { RoundedCornerShape(0.dp) },
                            effects = { blur(glass.radius.dp.toPx() * 1.5f) },
                            highlight = null,
                            shadow = null,
                            onDrawSurface = {
                                drawRect(
                                    if (glass.dark) Color.Black.copy(alpha = 0.55f)
                                    else Color.White.copy(alpha = 0.45f),
                                )
                            },
                        )
                    } else {
                        Modifier.background(
                            if (glass?.dark != false) Color.Black.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.5f),
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                modifier = modifier.padding(24.dp),
                shape = shape,
                tint = if (glass?.dark != false) Color.Black.copy(alpha = 0.34f)
                else Color.White.copy(alpha = 0.55f),
            ) {
                content()
            }
        }
    }
}
