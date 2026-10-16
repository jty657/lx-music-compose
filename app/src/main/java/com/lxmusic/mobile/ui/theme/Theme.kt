package com.lxmusic.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

val LocalThemeMode = compositionLocalOf { "system" }

@Composable
fun LxTheme(
    themeMode: String = "system",
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
    CompositionLocalProvider(LocalThemeMode provides themeMode) {
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
