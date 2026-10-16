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
 * 应用主题。
 * @param fontScale 全局字体缩放（设置项 common.fontScale），经 LocalDensity 实现：
 *   sp/dp 都按比例缩放，比逐个改 fontSize 更彻底
 */
@Composable
fun LxTheme(
    themeMode: String = "system",
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val controller = remember(themeMode) {
        when (themeMode) {
            "light" -> ThemeController(ColorSchemeMode.Light)
            "dark" -> ThemeController(ColorSchemeMode.Dark)
            "monet" -> ThemeController(ColorSchemeMode.MonetSystem, colorSpec = ThemeColorSpec.Spec2021, paletteStyle = ThemePaletteStyle.Content)
            else -> ThemeController(ColorSchemeMode.System)
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
