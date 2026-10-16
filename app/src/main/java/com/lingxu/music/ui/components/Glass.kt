package com.lingxu.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * 玻璃效果上下文。由 [GlassHost] 创建并通过 [LocalGlass] 下发。
 *
 * **关于 backdrop 的采样边界（重要，曾因误用导致闪退）**：
 * `Modifier.layerBackdrop(x)` 会把该节点自身的内容录进 x 的图层，而 `Modifier.drawBackdrop(x)`
 * 读取该图层重绘。因此 **layerBackdrop 的子树里不能出现 drawBackdrop 元素**，否则会自己采样
 * 自己 → 渲染树无限递归 → RenderThread 栈溢出闪退（SIGSEGV, 512 帧）。
 *
 * 结论：[content] 只应套在「纯背景内容」上；所有玻璃面必须是它的**兄弟节点**。
 * 页面内部的卡片、顶栏与内容同属一棵子树，因此它们**只能用半透明纯色**，不能用 backdrop。
 */
class GlassContext(
    val content: LayerBackdrop,
    val dark: Boolean,
    val radius: Float,
    /** API 33+ 才为 true：折射与方向光高光依赖 RuntimeShader */
    val refraction: Boolean,
)

val LocalGlass = staticCompositionLocalOf<GlassContext?> { null }

/** 玻璃面色板：不依赖 backdrop，用半透明填充 + 高光描边模拟磨砂玻璃 */
private fun glassFill(dark: Boolean): Color =
    if (dark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.58f)

private fun glassBorder(dark: Boolean): Color =
    if (dark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.75f)

/**
 * 玻璃容器：创建 backdrop 并向下游提供 [LocalGlass]。
 *
 * **调用方必须把 `glass.content` 通过 `Modifier.layerBackdrop(...)` 只套在背景内容上**，
 * 且保证所有使用 backdrop 的玻璃面（底栏、迷你播放条）是它的兄弟节点。
 * 页面内部的卡片/顶栏不参与 backdrop。
 */
@Composable
fun GlassHost(
    dark: Boolean,
    radius: Float,
    refraction: Boolean,
    content: @Composable (GlassContext) -> Unit,
) {
    val backdrop = rememberLayerBackdrop {
        drawRect(if (dark) Color.Black else Color.White)
        drawContent()
    }
    val context = remember(dark, radius, refraction) {
        GlassContext(backdrop, dark, radius, refraction)
    }
    CompositionLocalProvider(LocalGlass provides context) {
        content(context)
    }
}

/**
 * 玻璃卡片（页面内列表项 / 分组）。
 *
 * 页面内容与它同处 layerBackdrop 子树内，用 backdrop 会自采样递归，因此这里用
 * 半透明填充 + 高光描边的「拟玻璃」外观，视觉统一且无渲染风险。
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
    val dark = LocalGlass.current?.dark ?: false
    val interaction = remember { MutableInteractionSource() }
    val clickModifier = when {
        onClick != null && onLongClick != null -> Modifier.combinedClickable(
            interactionSource = interaction,
            indication = null,
            onLongClick = onLongClick,
            onClick = onClick,
        )
        onClick != null -> Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
        )
        else -> Modifier
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(tint ?: glassFill(dark), shape)
            .border(1.dp, glassBorder(dark), shape)
            .then(clickModifier),
        content = content,
    )
}

/** 玻璃顶栏（页面内标题条）。同 [GlassSurface]，用半透明纯色而非 backdrop */
@Composable
fun GlassTopBar(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = LocalGlass.current?.dark ?: false
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (dark) Color.Black.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.72f),
            ),
        content = content,
    )
}

/**
 * 玻璃弹窗：全屏半透明 scrim + 居中圆角面板。
 *
 * 弹窗位于独立窗口，跨窗口拿不到主窗口的 backdrop 图层，因此这里也不使用 backdrop，
 * 而是用半透明面板实现一致的观感。
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = LocalGlass.current?.dark ?: false
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (dark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.32f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = modifier
                    .padding(24.dp)
                    .clip(shape)
                    .background(
                        if (dark) Color(0xFF232323) else Color(0xFFF7F7F7),
                        shape,
                    )
                    .border(1.dp, glassBorder(dark), shape),
            ) {
                content()
            }
        }
    }
}
