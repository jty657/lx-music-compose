package com.lingxu.music.ui.theme

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset

/**
 * 「减弱动效」开关（Android 上对应「动画时长缩放 = 关闭」或无障碍里的移除动画）。
 *
 * 装饰性动效（封面呼吸、播放音柱、按压缩放）在开启时应退化为静帧：
 * 一是无障碍要求（前庭功能障碍用户对持续动画敏感），二是省掉每帧调度。
 * 一次性读取即可 —— 该开关变更会重建 Activity，届时重新求值。
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

/** 读取系统动画时长缩放：为 0 时视为要求减弱动效（[ValueAnimator.areAnimatorsEnabled] 需 API 26+） */
fun systemReduceMotion(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        !ValueAnimator.areAnimatorsEnabled()
    } else {
        false
    }

@Composable
fun rememberReduceMotion(): Boolean = remember { systemReduceMotion() }

/** 减弱动效时把时长压到 0（保留调用点结构，避免到处写 if） */
fun Int.motionMs(reduceMotion: Boolean): Int = if (reduceMotion) 0 else this

/**
 * 短促交互的统一时长（按压、图标切换、选中态着色）。
 * 150–200ms 是 Material「small component」区间：短到不拖沓，长到能被眼睛捕捉。
 */
val MotionFastMs: Int = 160

/** 中等时长：页面元素进出、列表增删、弹窗面板（180–260ms） */
val MotionBaseMs: Int = 240

/**
 * 全局统一的手感参数。
 *
 * 此前各调用点各写一套（按压有 0.55/420f、0.7/900f、0.88 缩放、0.98 缩放……），
 * 同一屏内「按下卡片」和「按下图标」的回弹速度不一致，整体显得零散。
 * 这里集中两档：
 * - [press]：高频、小幅（0.96~0.98），要求「按下即到位、松手不拖尾」→ 高刚度；
 * - [emphasized]：低频、明显（主播放键、弹窗面板），要求有回弹感 → 低刚度。
 */
object Motion {
    /** 按压回弹：高刚度、略欠阻尼，松手有一次很轻的回弹 */
    fun <T> press(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.72f, stiffness = 900f)

    /** 强调回弹：主按钮、面板进出，回弹更明显 */
    fun <T> emphasized(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.62f, stiffness = 420f)

    /** 列表增删/位移：比默认 spring(stiffness=1000f) 更柔和，避免增删时「弹一下」 */
    fun itemPlacement(): FiniteAnimationSpec<IntOffset> =
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold,
        )

    /** 列表项淡入淡出：短促线性淡变，配合位移使用 */
    fun itemFade(): FiniteAnimationSpec<Float> = tween(durationMillis = 180)
}

/**
 * 列表项进出/位移的统一入口：把 [Motion.itemFade] / [Motion.itemPlacement] 与
 * 「减弱动效」开关绑在一起。系统关闭动画时长缩放时一帧到位，与全站封面、按压缩放、
 * 弹窗进出的静帧降级保持一致；同时省掉低端机列表滚动时无意义的逐帧调度。
 *
 * 此前各列表虽已使用 [Motion.itemFade] / [Motion.itemPlacement]，但都绕过了
 * LocalReduceMotion —— 是全站动效降级覆盖里唯一的一组遗漏。
 */
@Composable
fun LazyItemScope.animateListItem(): Modifier {
    val reduceMotion = LocalReduceMotion.current
    val fade: FiniteAnimationSpec<Float> = if (reduceMotion) tween(0) else Motion.itemFade()
    val placement: FiniteAnimationSpec<IntOffset> =
        if (reduceMotion) tween(0) else Motion.itemPlacement()
    return Modifier.animateItem(fadeInSpec = fade, placementSpec = placement, fadeOutSpec = fade)
}
