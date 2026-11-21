package com.lingxu.music.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.MotionBaseMs
import com.lingxu.music.ui.theme.MotionFastMs

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
    // 深色下略提透明度（0.07 → 0.09）：原值在纯黑背景上对比只有约 1.15:1，
    // 卡片与背景几乎融成一片；0.09 后卡片边界可辨，又不至于变成「灰块」。
    if (dark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.58f)

private fun glassBorder(dark: Boolean): Color =
    if (dark) Color.White.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.75f)

/** 圆角尺度统一入口：来自 [com.lingxu.music.ui.theme.Dimens]，卡片 20dp，弹窗 28dp */
private val CardShape = RoundedCornerShape(Dimens.CardCorner)
private val DialogShape = RoundedCornerShape(Dimens.CornerXL)

/**
 * 系统「移除动画」（无障碍 → 移除动画 / 动画时长缩放 = 0）时返回 true。
 *
 * Compose 自身的 animate*AsState 会遵循系统动画时长缩放，但这里**手写**的按压缩放
 * 与进场标记（`shown` 状态）不会：在关闭动画的设备上它们仍会播放一次。
 * 对这类动效显式短路，既省掉每帧调度，也保证「关掉动画 = 立刻到位」。
 */
@Composable
private fun animationsDisabled(): Boolean = LocalReduceMotion.current

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
 *
 * 按压反馈两层叠加（对齐 LX Music 手机版列表项手感）：
 * 1. 系统 indication 涟漪（保留）；
 * 2. 填充色自动加深一档（[MotionFastMs] 颜色过渡，不再「按下去毫无变化」）+ 整卡缩到 0.98。
 *
 * 之前这里叠了三层（涟漪 + 变深 + 缩放 0.98 @ stiffness 900），按住横向连排的卡片时
 * 三张一起内收、幅度叠加，观感偏「重」。本轮把缩放收到 0.985 并改用统一的
 * [com.lingxu.music.ui.theme.Motion.press] 规格，让「按下」是轻触而不是塌陷。
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = LocalGlass.current?.dark ?: false
    val interaction = remember { MutableInteractionSource() }
    // 用系统默认 indication 给卡片按压反馈；此前 indication=null 导致全 App 点击无任何涟漪。
    // 外层 .clip(shape) 会把涟漪裁进圆角内，不会溢出边界。
    val indication = LocalIndication.current
    val pressed by interaction.collectIsPressedAsState()
    // 选中态（搜索页音源 chip 等）由 tint 表达：颜色渐变而非硬切，选中/取消更连贯
    val baseFill = tint ?: glassFill(dark)
    val fill by animateColorAsState(
        targetValue = if (pressed) {
            // 深色：叠白提亮；浅色：叠黑压暗。
            // 有 tint 的胶囊（选中态）在浅色下叠黑、深色下叠白，方向都正确；
            // 但浅色下 tint 本身是主色淡底，叠黑会让「选中 + 按下」看起来像被取消，
            // 所以浅色 + tint 时改用主色加深，保留「选中感」。
            when {
                tint != null && !dark -> baseFill.copy(alpha = (baseFill.alpha + 0.10f).coerceAtMost(1f))
                dark -> androidx.compose.ui.graphics.lerp(baseFill, Color.White.copy(alpha = 0.16f), 0.5f)
                else -> androidx.compose.ui.graphics.lerp(baseFill, Color.Black.copy(alpha = 0.05f), 0.5f)
            }
        } else {
            baseFill
        },
        animationSpec = tween(durationMillis = MotionFastMs),
        label = "glassFill",
    )
    // 按压缩放：只动 transform，不触发重新布局；系统关动画时不动（避免「关不掉」的缩放）
    val noAnim = animationsDisabled()
    val scale by animateFloatAsState(
        // 0.985 而非 0.98：卡片面积大，2% 的收缩已经很「实」，1.5% 更接近 iOS/MIUI 的轻触感
        targetValue = if (pressed && onClick != null && !noAnim) 0.985f else 1f,
        animationSpec = Motion.press(),
        label = "glassPress",
    )
    val clickModifier = when {
        onClick != null && onLongClick != null -> Modifier.combinedClickable(
            interactionSource = interaction,
            indication = indication,
            onLongClick = onLongClick,
            onClick = onClick,
        )
        onClick != null -> Modifier.clickable(
            interactionSource = interaction,
            indication = indication,
            onClick = onClick,
        )
        else -> Modifier
    }
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(fill, shape)
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
            // 顶栏底部加一道 1px 分隔线：此前顶栏与首条内容同色，滚动时内容会「贴上」标题区，
            // 缺少边界（浅色 4%、深色 8%，明显但不抢视觉）。
            .drawBehind {
                val lineH = Dimens.Hairline.toPx()
                drawRect(
                    color = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - lineH),
                    size = androidx.compose.ui.geometry.Size(size.width, lineH),
                )
            }
            .background(
                // 与玻璃卡片同极性：深色用低透明白叠在暗背景上，避免「黑顶栏 + 白卡片」的明暗割裂
                if (dark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.72f),
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
    shape: Shape = DialogShape,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = LocalGlass.current?.dark ?: false
    // 弹窗是独立窗口，无法跨窗口做共享元素转场；用一次性淡入 + 轻微放大，
    // 避免「啪」地整块出现。shown 只在首次组合后置 true，不会随内容重组重播。
    var shown by remember { mutableStateOf(false) }
    // 关闭请求先落到 closing：先播 [MotionFastMs] 退出动画，动画结束再回调真正的 onDismiss。
    // 此前点「取消」是直接消失，与入场动画完全不对称。
    var closing by remember { mutableStateOf(false) }
    val noAnim = animationsDisabled()
    LaunchedEffect(Unit) { shown = true }
    LaunchedEffect(closing) {
        if (closing) {
            // 与 exit 动画（fadeOut 100ms）对齐：稍等一帧再关窗，避免面板被硬切。
            // 但手写 delay 不遵循系统「动画时长缩放」：关闭动画的设备上必须立即关窗，
            // 否则会空等 MotionFastMs*3/4 才 dismiss，与「关掉动画=立刻到位」矛盾。
            if (!noAnim) kotlinx.coroutines.delay(MotionFastMs.toLong() * 3 / 4)
            onDismissRequest()
        }
    }
    val scrimColor = if (dark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.32f)
    val scrimAlpha by animateFloatAsState(
        targetValue = if (shown && !closing) 1f else 0f,
        animationSpec = tween(durationMillis = MotionFastMs),
        label = "dialogScrim",
    )
    Dialog(
        onDismissRequest = {
            // 系统返回键 / 点外部也走退出动画；重复请求只认第一次
            if (!closing) closing = true
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        // 窗口级淡入：Dialog 窗口创建时 decorView 默认 alpha=1，若等 LaunchedEffect 再压到 0.25，
        // 首帧就已经把整块面板画出来了 → 「点播放设置先闪一下黑底」。
        // 这里用 DisposableEffect 在attach 时同步设置，并在离场时还原，保证没有中间态。
        //
        // 同时关掉系统 dim：改由自己的 scrim 承担遮罩，避免「系统 dim + 自绘 scrim」两层变暗。
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        DisposableEffect(window) {
            window?.setDimAmount(0f)
            onDispose { window?.decorView?.alpha = 1f }
        }
        LaunchedEffect(scrimAlpha, window, noAnim) {
            // 入场进度 0.25 → 1：留 25% 的底给缩放入场的面板，避免首帧完全透明导致「从无到有」的跳变
            window?.decorView?.alpha = if (noAnim) 1f else 0.25f + 0.75f * scrimAlpha
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = scrimColor.alpha * scrimAlpha)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = shown && !closing,
                enter = fadeIn(tween(MotionFastMs)) +
                    scaleIn(initialScale = 0.94f, animationSpec = Motion.emphasized()),
                exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.96f, animationSpec = tween(120)),
            ) {
                Box(
                    modifier = modifier
                        .padding(24.dp)
                        .clip(shape)
                        .background(
                            if (dark) Color(0xFF232323) else Color(0xFFF7F7F7),
                            shape,
                        )
                        .border(1.dp, glassBorder(dark), shape)
                        // 选项多的弹窗在小屏上会溢出，允许纵向滚动避免内容被截断
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }
            }
        }
    }
}
