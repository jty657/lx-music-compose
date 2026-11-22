package com.lingxu.music.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.FilterChip
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.TextActionButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.player.PlayMode
import kotlinx.coroutines.flow.map
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

    // 检查更新结果（独立弹窗，避免复用「备份」标题）
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateTitle by remember { mutableStateOf("") }
    var updateBody by remember { mutableStateOf("") }
    var updateUrl by remember { mutableStateOf("") }
    var updateChecking by remember { mutableStateOf(false) }

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

    // 恢复是覆盖式（备份里没有的列表会被删除），先让用户确认
    var showImportConfirm by remember { mutableStateOf(false) }

    // 不喜欢列表编辑
    var showDislike by remember { mutableStateOf(false) }
    var newDislike by remember { mutableStateOf("") }

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
    // 滑块拖动中的本地值：落盘已节流，拖动时用它驱动显示。
    // 不再用 LaunchedEffect 把节流回写的持久化值灌回来——那会在拖动过程中把滑块拽回旧值造成回弹抖动；
    // 初值取自持久化值，最终值由 onDispose 的 commit* 补写，外部变更在下次进入页面时自然生效。
    var localRate by remember { mutableFloatStateOf(playbackRate) }
    var localVolume by remember { mutableFloatStateOf(volume) }
    val timeoutExitPlayed by viewModel.timeoutExitPlayed.collectAsState(initial = true)
    // 只订阅定时剩余秒数这一个字段，而不是整个 PlayState（positionMs 播放中每 500ms 变化，
    // 订阅整个 state 会让本页几十个 Preference/Slider 每 500ms 全量重组）。
    val timeoutRemaining by remember {
        LxApp.app.playerRepository.state.map { it.timeoutExitRemaining }
    }.collectAsState(initial = -1)
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
    // 拖动中的本地值：每帧写盘会重建 GlassContext 让整屏 backdrop 重画，落盘已节流
    // 同倍速/音量：不用 LaunchedEffect 回灌持久化值，避免拖动过程中滑块回弹
    var localBlur by remember { mutableFloatStateOf(blurRadius) }
    val userApiLog by viewModel.userApiLog.collectAsState(initial = false)

    // 离开设置页时提交倍速/音量节流跳过的最终值，避免拖完立刻返回导致调整丢失
    DisposableEffect(Unit) {
        onDispose {
            viewModel.commitPlaybackRate()
            viewModel.commitVolume()
        }
    }

    // 设置页内容很长且是单列偏好项，平板上会拉成一条超宽横线：限制内容宽度并居中。
    // 手机竖屏（<840dp）下 widthIn 不产生任何效果，等价于原布局。
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
        Modifier
            .widthIn(max = ContentMaxWidth)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        // 与其它二级页统一顶栏样式（此前设置页是唯一用裸 Text 作标题的页面）
        GlassTopBar {
            Text(
                "设置",
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            )
        }
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
        // 字体大小用分段选择：滑块每拖一帧都要写 DataStore 并重排整个界面，
        // 而字号本来也只有几档常用值。用 SectionTitle 与周围 Preference 的标题层级统一。
        SectionTitle("字体大小")
        Spacer(Modifier.size(4.dp))
        // 分段选择器：此前选中项被 enabled=false 置灰，与「当前选中」的语义正好相反
        // （用户会以为那一档不可用）。改为 FilterChip 的「选中高亮、其余可点」，
        // 并对当前值额外做语义标记，读屏可播报「已选中」。
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f).forEach { scale ->
                val selected = (fontScale - scale).let { it < 0.06f && it > -0.06f }
                FilterChip(
                    label = "${(scale * 100).toInt()}%",
                    selected = selected,
                    // 点已选中的档位不做任何事，避免无意义的重排
                    onClick = { if (!selected) viewModel.setFontScale(scale) },
                    horizontalPadding = 12,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        ArrowPreference(
            title = "分享方式",
            summary = when (shareType) {
                "clipboard" -> "复制到剪贴板"
                else -> "系统分享"
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
            summary = "${(localRate * 100).toInt()}%",
            value = localRate,
            valueRange = 0.5f..3f,
            onValueChange = {
                localRate = it
                viewModel.setPlaybackRateAndApply(it)
            },
        )
        SliderPreference(
            title = "音量",
            summary = "${(localVolume * 100).toInt()}%",
            value = localVolume,
            valueRange = 0f..1f,
            onValueChange = {
                localVolume = it
                viewModel.setVolumeAndApply(it)
            },
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
                summary = "${localBlur.toInt()}dp",
                value = localBlur,
                onValueChange = {
                    localBlur = it
                    viewModel.setBlurRadius(it)
                },
                valueRange = 8f..32f,
            )
            // 松手后提交最终值
            DisposableEffect(Unit) {
                onDispose { viewModel.commitBlurRadius() }
            }
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
            summary = "把收藏与我的列表导出为 JSON 文件（不含设置与音源脚本）",
            onClick = { exportLauncher.launch(LxApp.app.backupRepository.defaultFileName()) },
        )
        ArrowPreference(
            title = "导入配置",
            summary = "从 JSON 文件恢复收藏与我的列表（会覆盖当前数据）",
            onClick = { showImportConfirm = true },
        )

        Spacer(Modifier.size(12.dp))

        // ============ 其他 ============
        SectionTitle("其他")
        ArrowPreference(
            title = "不喜欢列表",
            summary = "命中的歌曲在切歌时自动跳过（格式：歌曲名@歌手，也可只写歌曲名或 @歌手）",
            onClick = { showDislike = true },
        )
        ArrowPreference(
            title = "清除缓存",
            summary = "清理已缓存的封面图片等网络数据",
            onClick = {
                val size = com.lingxu.music.core.request.HttpClient.instance.diskCacheSizeBytes()
                com.lingxu.music.core.request.HttpClient.instance.clearDiskCache()
                toast = if (size > 0) "已清理 ${size / 1024} KB 缓存" else "没有可清理的缓存"
            },
        )

        // ============ 系统能力（对齐原版 utils / 版本更新源） ============
        SectionTitle("系统")
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val batteryIgnored by viewModel.batteryIgnored.collectAsState(initial = false)
        ArrowPreference(
            title = "忽略电池优化",
            summary = if (batteryIgnored) "已加入白名单，后台播放更稳定" else "加入白名单，避免后台播放被系统冻结",
            onClick = {
                val ok = com.lingxu.music.core.system.BatteryOptimization.requestIgnore(ctx)
                if (!ok) toast = "当前系统不支持该功能"
                viewModel.refreshBatteryIgnored()
            },
        )
        ArrowPreference(
            title = "检查更新",
            summary = "检查是否有新版本",
            onClick = {
                updateChecking = true
                showUpdateDialog = true
                updateTitle = "检查更新中…"
                updateBody = "正在连接更新源"
                updateUrl = ""
                val current = runCatching {
                    ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
                }.getOrNull() ?: ""
                viewModel.checkUpdate(current) { title, body, url ->
                    updateTitle = title
                    updateBody = body
                    updateUrl = url
                    updateChecking = false
                }
            },
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
        Spacer(Modifier.size(8.dp))
        val deviceLines = androidx.compose.runtime.remember {
            val d = com.lingxu.music.core.system.DeviceInfo
            listOf(
                "设备：${d.model()}",
                "系统：${d.androidVersion()}",
                "架构：${d.abi()}",
                "语言：${d.language()}",
            )
        }
        deviceLines.forEach { line ->
            Text(
                line,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }

        Spacer(Modifier.size(24.dp))
    }
    if (toast != null) {
        GlassDialog(onDismissRequest = { toast = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("提示", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(toast!!)
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { toast = null }) { Text("好") }
                }
            }
        }
    }
    if (showUpdateDialog) {
        val updateCtx = androidx.compose.ui.platform.LocalContext.current
        GlassDialog(onDismissRequest = { showUpdateDialog = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(updateTitle, fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(updateBody)
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (!updateChecking) {
                        // 发现新版本且拿到下载地址时给出直达入口；此前只显示一句「好」，
                        // 用户即便知道有新版本也无从下载。
                        if (updateUrl.isNotBlank()) {
                            Button(onClick = {
                                runCatching {
                                    updateCtx.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(updateUrl),
                                        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                                showUpdateDialog = false
                            }) { Text("前往下载") }
                            Spacer(Modifier.size(8.dp))
                        }
                        Button(onClick = { showUpdateDialog = false }) { Text("好") }
                    }
                }
            }
        }
    }
    if (showDislike) {
        val rules by viewModel.dislikeRules.collectAsState()
        GlassDialog(onDismissRequest = { showDislike = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("不喜欢列表", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = newDislike,
                    onValueChange = { newDislike = it },
                    label = { Text("歌曲名@歌手") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        viewModel.addDislike(newDislike)
                        newDislike = ""
                    }) { Text("添加") }
                }
                Spacer(Modifier.size(8.dp))
                if (rules.isEmpty()) {
                    Text(
                        "暂无规则",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                } else {
                    rules.forEach { rule ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(rule, modifier = Modifier.weight(1f))
                            // 统一走 TextActionButton：48dp 命中区 + 按压缩放 + Role.Button + 静帧降级
                            TextActionButton(
                                text = "删除",
                                onClick = { viewModel.removeDislike(rule) },
                                color = MiuixTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showImportConfirm) {
        GlassDialog(onDismissRequest = { showImportConfirm = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("导入配置", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(
                    "将用备份文件覆盖当前的收藏与我的列表；备份里没有的列表会被删除。",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { showImportConfirm = false }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        showImportConfirm = false
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }) { Text("继续") }
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
                            // 纯文字选项热区补到约 48dp（与搜索页/不喜欢列表同类项一致），字号不变
                            .padding(vertical = 14.dp),
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
                            // 同上：热区补到约 48dp
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun SectionTitle(text: String) {
    // 分区标题放大一档并用 primary 色，与正文 summary 拉开层次，便于快速扫读
    Text(
        text,
        fontSize = MiuixTheme.textStyles.body1.fontSize,
        color = MiuixTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
    )
    Spacer(Modifier.size(4.dp))
}
