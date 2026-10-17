package com.lingxu.music.ui.screens.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.lingxu.music.ui.components.GlassDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.player.PlayMode
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 设置页：分组对齐原版 Basic / Player / Lyric / Search / List / 液态玻璃 / 自定义音源。
 * 同步备份恢复走系统文件选择器（导出 JSON / 导入 JSON）。
 */
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<String?>(null) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            LxApp.app.backupRepository.export(uri)
                .onSuccess { count -> toast = "已导出 $count 条" }
                .onFailure { toast = "导出失败：${it.message}" }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            LxApp.app.backupRepository.import(uri)
                .onSuccess { count -> toast = "已恢复 $count 条" }
                .onFailure { toast = "恢复失败：${it.message}" }
        }
    }

    val themeMode by viewModel.themeMode.collectAsState(initial = "system")
    val lang by viewModel.lang.collectAsState(initial = "system")
    val fontScale by viewModel.fontScale.collectAsState(initial = 1f)
    val shareType by viewModel.shareType.collectAsState(initial = "system")
    val autoHidePlayBar by viewModel.autoHidePlayBar.collectAsState(initial = true)

    val quality by viewModel.quality.collectAsState(initial = "128k")
    val playMode by viewModel.playMode.collectAsState(initial = "listLoop")
    val startupAutoPlay by viewModel.startupAutoPlay.collectAsState(initial = false)
    val isSavePlayTime by viewModel.isSavePlayTime.collectAsState(initial = false)
    val isHandleAudioFocus by viewModel.isHandleAudioFocus.collectAsState(initial = true)
    val playbackRate by viewModel.playbackRate.collectAsState(initial = 1f)
    val volume by viewModel.volume.collectAsState(initial = 1f)
    val timeoutExitPlayed by viewModel.timeoutExitPlayed.collectAsState(initial = true)
    val playState by LxApp.app.playerRepository.state.collectAsState()
    val timeoutRemaining = playState.timeoutExitRemaining
    var showTimeoutExit by remember { mutableStateOf(false) }

    val showLyricTranslation by viewModel.showLyricTranslation.collectAsState(initial = true)
    val showLyricRoma by viewModel.showLyricRoma.collectAsState(initial = false)
    val lyricAlign by viewModel.lyricAlign.collectAsState(initial = "left")
    val lyricFontSize by viewModel.lyricFontSize.collectAsState(initial = 1f)
    val isZoomActiveLrc by viewModel.isZoomActiveLrc.collectAsState(initial = false)

    val isShowHotSearch by viewModel.isShowHotSearch.collectAsState(initial = true)
    val isShowHistorySearch by viewModel.isShowHistorySearch.collectAsState(initial = true)

    val isShowSource by viewModel.isShowSource.collectAsState(initial = true)
    val isShowAlbumName by viewModel.isShowAlbumName.collectAsState(initial = false)
    val isShowInterval by viewModel.isShowInterval.collectAsState(initial = true)
    val addMusicLocation by viewModel.addMusicLocation.collectAsState(initial = "bottom")

    val blurEnabled by viewModel.blurEnabled.collectAsState(initial = true)
    val blurRadius by viewModel.blurRadius.collectAsState(initial = 24f)
    val userApiLog by viewModel.userApiLog.collectAsState(initial = false)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        Text("设置", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(12.dp))

        // ============ 基本设置 ============
        SectionTitle("基本设置")
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
        ArrowPreference(
            title = "语言",
            summary = when (lang) {
                "system" -> "跟随系统"
                "zh_cn" -> "简体中文"
                "zh_tw" -> "繁體中文"
                "en" -> "English"
                else -> lang
            } + "（界面文案暂仅简体中文）",
            onClick = { viewModel.cycleLang() },
        )
        SliderPreference(
            title = "字体大小",
            summary = "${(fontScale * 100).toInt()}%",
            value = fontScale,
            onValueChange = { viewModel.setFontScale(it) },
            valueRange = 0.8f..1.6f,
        )
        ArrowPreference(
            title = "分享方式",
            summary = when (shareType) {
                "system" -> "系统分享"
                "link" -> "分享链接"
                "text" -> "分享文本"
                else -> shareType
            },
            onClick = { viewModel.cycleShareType() },
        )
        SwitchPreference(
            title = "自动隐藏播放条",
            summary = "滚动列表时自动隐藏底部迷你播放条",
            checked = autoHidePlayBar,
            onCheckedChange = { viewModel.setAutoHidePlayBar(it) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 播放设置 ============
        SectionTitle("播放设置")
        ArrowPreference(
            title = "播放音质",
            summary = quality,
            onClick = { viewModel.cycleQuality() },
        )
        ArrowPreference(
            title = "播放模式",
            summary = PlayMode.label(playMode),
            onClick = { viewModel.cyclePlayMode() },
        )
        SwitchPreference(
            title = "启动时自动播放",
            summary = "打开 App 后自动开始播放上次的歌曲",
            checked = startupAutoPlay,
            onCheckedChange = { viewModel.setStartupAutoPlay(it) },
        )
        SwitchPreference(
            title = "保存播放进度",
            summary = "记住每首歌的播放位置",
            checked = isSavePlayTime,
            onCheckedChange = { viewModel.setIsSavePlayTime(it) },
        )
        SwitchPreference(
            title = "音频焦点处理",
            summary = "来电或其它 App 播放时自动暂停",
            checked = isHandleAudioFocus,
            onCheckedChange = { viewModel.setIsHandleAudioFocus(it) },
        )
        SliderPreference(
            title = "播放倍速",
            summary = "${(playbackRate * 100).toInt()}%",
            value = playbackRate,
            valueRange = 0.5f..3f,
            onValueChange = { viewModel.setPlaybackRateAndApply(it) },
        )
        SliderPreference(
            title = "音量",
            summary = "${(volume * 100).toInt()}%",
            value = volume,
            valueRange = 0f..1f,
            onValueChange = { viewModel.setVolumeAndApply(it) },
        )
        ArrowPreference(
            title = "定时停止播放",
            summary = if (timeoutRemaining > 0) {
                "剩余 ${timeoutRemaining / 60}分${timeoutRemaining % 60}秒"
            } else {
                "未开启"
            },
            onClick = { showTimeoutExit = true },
        )
        SwitchPreference(
            title = "播完当前歌曲再停",
            summary = "定时到点后等当前歌曲播完再停止",
            checked = timeoutExitPlayed,
            onCheckedChange = { viewModel.setTimeoutExitPlayed(it) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 歌词设置 ============
        SectionTitle("歌词设置")
        SwitchPreference(
            title = "显示翻译歌词",
            summary = "优先展示 tlyric 翻译",
            checked = showLyricTranslation,
            onCheckedChange = { viewModel.setShowLyricTranslation(it) },
        )
        SwitchPreference(
            title = "显示罗马音",
            summary = "展示 rlyric 罗马音译",
            checked = showLyricRoma,
            onCheckedChange = { viewModel.setShowLyricRoma(it) },
        )
        ArrowPreference(
            title = "歌词对齐",
            summary = if (lyricAlign == "center") "居中" else "左对齐",
            onClick = { viewModel.cycleLyricAlign() },
        )
        SliderPreference(
            title = "歌词字号",
            summary = "${(lyricFontSize * 100).toInt()}%",
            value = lyricFontSize,
            onValueChange = { viewModel.setLyricFontSize(it) },
            valueRange = 0.8f..1.8f,
        )
        SwitchPreference(
            title = "放大当前歌词",
            checked = isZoomActiveLrc,
            onCheckedChange = { viewModel.setIsZoomActiveLrc(it) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 搜索设置 ============
        SectionTitle("搜索设置")
        SwitchPreference(
            title = "显示热搜",
            summary = "在首页展示平台热搜词",
            checked = isShowHotSearch,
            onCheckedChange = { viewModel.setIsShowHotSearch(it) },
        )
        SwitchPreference(
            title = "显示搜索历史",
            checked = isShowHistorySearch,
            onCheckedChange = { viewModel.setIsShowHistorySearch(it) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 列表设置 ============
        SectionTitle("列表设置")
        SwitchPreference(
            title = "显示音源",
            summary = "歌曲条目展示来源平台",
            checked = isShowSource,
            onCheckedChange = { viewModel.setIsShowSource(it) },
        )
        SwitchPreference(
            title = "显示专辑名",
            checked = isShowAlbumName,
            onCheckedChange = { viewModel.setIsShowAlbumName(it) },
        )
        SwitchPreference(
            title = "显示时长",
            checked = isShowInterval,
            onCheckedChange = { viewModel.setIsShowInterval(it) },
        )
        ArrowPreference(
            title = "添加歌曲位置",
            summary = if (addMusicLocation == "top") "添加到顶部" else "添加到底部",
            onClick = { viewModel.cycleAddMusicLocation() },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 液态玻璃 ============
        SectionTitle("液态玻璃")
        SwitchPreference(
            title = "背景模糊层",
            summary = "底部栏使用 Backdrop 液态玻璃（API 31+ 生效）",
            checked = blurEnabled,
            onCheckedChange = { viewModel.setBlurEnabled(it) },
        )
        if (blurEnabled) {
            SliderPreference(
                title = "模糊强度",
                summary = "${blurRadius.toInt()}dp",
                value = blurRadius,
                onValueChange = { viewModel.setBlurRadius(it) },
                valueRange = 8f..32f,
            )
        }

        Spacer(Modifier.size(12.dp))

        // ============ 自定义音源 ============
        SectionTitle("自定义音源")
        SwitchPreference(
            title = "记录自定义源日志",
            summary = "启用后记录脚本 console 输出",
            checked = userApiLog,
            onCheckedChange = { viewModel.setUserApiLog(it) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 同步、备份与恢复 ============
        SectionTitle("同步、备份与恢复")
        ArrowPreference(
            title = "导出配置",
            summary = "把设置、收藏、我的列表导出为 JSON 文件",
            onClick = { exportLauncher.launch(LxApp.app.backupRepository.defaultFileName()) },
        )
        ArrowPreference(
            title = "导入配置",
            summary = "从 JSON 文件恢复设置与列表",
            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 关于 ============
        SectionTitle("关于")
        val appContext = androidx.compose.ui.platform.LocalContext.current
        val versionName = androidx.compose.runtime.remember {
            runCatching {
                appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
            }.getOrNull() ?: ""
        }
        Text(
            "聆序 $versionName",
            fontSize = MiuixTheme.textStyles.main.fontSize,
        )
        Text(
            "洛雪音乐 v1.9.1 的 Compose 移植版",
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Spacer(Modifier.size(24.dp))
    }
    if (toast != null) {
        GlassDialog(onDismissRequest = { toast = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("备份", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(toast!!)
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { toast = null }) { Text("好") }
                }
            }
        }
    }
    if (showTimeoutExit) {
        GlassDialog(onDismissRequest = { showTimeoutExit = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("定时停止播放", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                listOf(15, 30, 45, 60, 90, 120).forEach { min ->
                    Text(
                        "$min 分钟",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTimeoutExit(min)
                                showTimeoutExit = false
                            }
                            .padding(vertical = 10.dp),
                    )
                }
                if (timeoutRemaining > 0) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "取消定时",
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTimeoutExit(0)
                                showTimeoutExit = false
                            }
                            .padding(vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = MiuixTheme.textStyles.body2.fontSize)
    Spacer(Modifier.size(4.dp))
}
