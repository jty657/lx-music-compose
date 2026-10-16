package com.lxmusic.mobile.ui.screens.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 设置页：主题/音质/玻璃效果等（Miuix Preference 风格） */
@Composable
fun SettingScreen(viewModel: SettingViewModel = viewModel()) {
    val themeMode by viewModel.themeMode.collectAsState(initial = "system")
    val quality by viewModel.quality.collectAsState(initial = "128k")
    val blurEnabled by viewModel.blurEnabled.collectAsState(initial = true)
    val blurRadius by viewModel.blurRadius.collectAsState(initial = 24f)
    val userApiLog by viewModel.userApiLog.collectAsState(initial = false)
    val playMode by viewModel.playMode.collectAsState(initial = "listLoop")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        Text("设置", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(12.dp))

        Text("主题", fontSize = MiuixTheme.textStyles.body2.fontSize)
        ArrowPreference(
            title = "主题模式",
            summary = when (themeMode) {
                "system" -> "跟随系统"
                "light" -> "浅色"
                "dark" -> "深色"
                "monet" -> "动态取色"
                else -> themeMode
            },
            onClick = { viewModel.cycleTheme() },
        )

        Spacer(Modifier.size(12.dp))
        Text("播放", fontSize = MiuixTheme.textStyles.body2.fontSize)
        ArrowPreference(
            title = "播放音质",
            summary = quality,
            onClick = { viewModel.cycleQuality() },
        )
        ArrowPreference(
            title = "播放模式",
            summary = com.lxmusic.mobile.core.player.PlayMode.label(playMode),
            onClick = { viewModel.cyclePlayMode() },
        )

        Spacer(Modifier.size(12.dp))
        Text("液态玻璃", fontSize = MiuixTheme.textStyles.body2.fontSize)
        SwitchPreference(
            title = "背景模糊层",
            summary = "使用 Backdrop 液态玻璃效果",
            checked = blurEnabled,
            onCheckedChange = { viewModel.setBlurEnabled(it) },
        )
        if (blurEnabled) {
            top.yukonga.miuix.kmp.preference.SliderPreference(
                title = "模糊强度",
                summary = "${blurRadius.toInt()}dp",
                value = blurRadius,
                onValueChange = { viewModel.setBlurRadius(it) },
                valueRange = 8f..64f,
            )
        }

        Spacer(Modifier.size(12.dp))
        Text("自定义音源", fontSize = MiuixTheme.textStyles.body2.fontSize)
        SwitchPreference(
            title = "记录自定义源日志",
            summary = "启用后记录脚本 console 输出",
            checked = userApiLog,
            onCheckedChange = { viewModel.setUserApiLog(it) },
        )
    }
}
