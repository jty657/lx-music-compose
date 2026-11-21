package com.lingxu.music.ui.theme

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

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
