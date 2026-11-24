package com.lingxu.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

val LocalThemeMode = compositionLocalOf { "system" }

/**
 * 主题主色（设置项 `common.themeColor`，对齐原版「主题」里的多套皮肤）。
 *
 * 原版 lx-music-mobile 通过 `theme.id` 切换一整套配色（绿/蓝/新年红…）；
 * 移植版此前只有「跟随系统 / 浅色 / 深色 / 取色」四种**明暗模式**，没有任何主色可选。
 * 这里不照搬原版的多份色板文件（每套几十个色值，且与本项目 Miuix 色彩体系不兼容），
 * 而是交给 Miuix 由**一个主色**推导整套调色板：明暗两套、语义色（primary/onPrimary/
 * surface…）全部自动生成，深浅色对比度由库保证，改动面只有一个参数。
 *
 * `default` 表示不覆盖主色（保持 Miuix 默认配色，即升级前的外观），保证老用户观感不变。
 */
val ThemeColors: Map<String, Color?> = linkedMapOf(
    "default" to null,
    "green" to Color(0xFF3E9B6B),
    "blue" to Color(0xFF3B7DD8),
    "purple" to Color(0xFF7A5CD6),
    "orange" to Color(0xFFD9822B),
    "pink" to Color(0xFFD9537C),
)

/** 主色 id → 展示名（设置页循环切换时显示） */
fun themeColorLabel(id: String): String = when (id) {
    "green" -> "青绿"
    "blue" -> "靛蓝"
    "purple" -> "黛紫"
    "orange" -> "暖橙"
    "pink" -> "绯粉"
    else -> "默认"
}

/**
 * 应用主题。
 * @param themeMode 明暗模式：system / light / dark / monet
 * @param themeColor 主色 id（[ThemeColors] 的 key），default 表示不覆盖
 * @param fontScale 全局字体缩放（设置项 common.fontScale），经 LocalDensity 实现：
 *   sp/dp 都按比例缩放，比逐个改 fontSize 更彻底
 */
@Composable
fun LxTheme(
    themeMode: String = "system",
    themeColor: String = "default",
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val keyColor = ThemeColors[themeColor]
    val controller = remember(themeMode, keyColor) {
        when (themeMode) {
            // 取色模式下由系统壁纸决定主色，用户选的主色不再生效（与原版 Monet 行为一致）
            "monet" -> ThemeController(ColorSchemeMode.MonetSystem, colorSpec = ThemeColorSpec.Spec2021, paletteStyle = ThemePaletteStyle.Content)
            "light" -> if (keyColor == null) ThemeController(ColorSchemeMode.Light) else ThemeController(ColorSchemeMode.Light, keyColor = keyColor)
            "dark" -> if (keyColor == null) ThemeController(ColorSchemeMode.Dark) else ThemeController(ColorSchemeMode.Dark, keyColor = keyColor)
            else -> if (keyColor == null) ThemeController(ColorSchemeMode.System) else ThemeController(ColorSchemeMode.System, keyColor = keyColor)
        }
    }
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, fontScale) {
        Density(density = baseDensity.density, fontScale = baseDensity.fontScale * fontScale)
    }
    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalDensity provides scaledDensity,
    ) {
        MiuixTheme(controller = controller) {
            content()
        }
    }
}

@Composable
fun isInDarkTheme(): Boolean = when (LocalThemeMode.current) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
}
