package com.lingxu.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 全局尺寸/圆角/间距记号。
 *
 * 此前圆角散落在各处（8/10/12/20/22/28dp），同一个「胶囊 chip」在搜索页是 20dp、
 * 在播放页是 12dp，卡片又是 20dp —— 视觉语言没有统一层级。
 * 这里按「语义」而非「数值」命名，改一处即可全站生效。
 */
object Dimens {
    /** 页面横向内边距：列表页统一 12dp，播放页 20dp（沉浸页留白更大） */
    val PageH: Dp = 12.dp

    /** 卡片/列表面板圆角 */
    val CardCorner: Dp = 20.dp

    /** 小圆角：封面 8dp、进度条 1dp 等 */
    val CornerS: Dp = 8.dp
    val CornerM: Dp = 12.dp
    val CornerL: Dp = 20.dp
    val CornerXL: Dp = 28.dp

    /** 胶囊 chip 圆角：足够圆 => 视觉上是「可选项」而不是「按钮」 */
    val ChipCorner: Dp = 999.dp

    /** 列表项之间的纵向间隙 */
    val ItemGap: Dp = 4.dp

    /** 列表项内部 padding */
    val ItemPad: Dp = 8.dp

    /**
     * 空态 / 加载态的上下留白：全站空列表、加载中、重试提示共用同一垂直节奏。
     * 此前 EmptyHint / LoadingHint / RetryHint 各自硬编码 56dp，改一处即可整体对齐，
     * 避免后续某个页面单独调留白造成「空态忽高忽低」的漂移。
     */
    val EmptyStateVPadding: Dp = 56.dp

    /** 封面尺寸：列表行 44dp，迷你条 40dp，播放页自适应 */
    val CoverRow: Dp = 44.dp
    val CoverMini: Dp = 40.dp

    /** 无障碍最小点击热区 */
    val TouchTarget: Dp = 48.dp

    /** 卡片描边：1dp 高光，是「玻璃」质感的关键（纯色块 vs 玻璃的区别几乎全在这条线） */
    val Hairline: Dp = 1.dp
}

/** 胶囊形 chip 的圆角形状 */
val ChipShape = RoundedCornerShape(Dimens.ChipCorner)

/** 卡片形 */
val CardShapeExplicit = RoundedCornerShape(Dimens.CardCorner)
