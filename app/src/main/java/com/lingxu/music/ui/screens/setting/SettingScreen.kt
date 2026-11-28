package com.lingxu.music.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.dialogOptionClickable
import com.lingxu.music.ui.components.FilterChip
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.TextActionButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // 开源项目信息弹窗（对齐原版「关于」页的开源地址 / 最新版下载 / 常见问题入口）。
    // 纯展示 + 复用全站 GlassDialog / TextActionButton，不引入新依赖、不触碰播放与音源逻辑。
    var showAboutProject by remember { mutableStateOf(false) }

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
    val themeColor by viewModel.themeColor.collectAsState(initial = "default")
    val dynamicBg by viewModel.dynamicBg.collectAsState(initial = false)
    val hideBgDark by viewModel.hideBgDark.collectAsState(initial = false)
    val lang by viewModel.lang.collectAsState(initial = "system")
    val fontScale by viewModel.fontScale.collectAsState(initial = 1f)
    val shareType by viewModel.shareType.collectAsState(initial = "system")
    val autoHidePlayBar by viewModel.autoHidePlayBar.collectAsState(initial = true)
    val allowProgressBarSeek by viewModel.allowProgressBarSeek.collectAsState(initial = true)
    val sourceNameType by viewModel.sourceNameType.collectAsState(initial = "real")
    val showBackBtn by viewModel.showBackBtn.collectAsState(initial = true)

    val quality by viewModel.quality.collectAsState(initial = "128k")
    val playMode by viewModel.playMode.collectAsState(initial = "listLoop")
    val startupAutoPlay by viewModel.startupAutoPlay.collectAsState(initial = false)
    val startupPushPlayDetail by viewModel.startupPushPlayDetail.collectAsState(initial = false)
    val isSavePlayTime by viewModel.isSavePlayTime.collectAsState(initial = false)
    val isHandleAudioFocus by viewModel.isHandleAudioFocus.collectAsState(initial = true)
    val isShowNotificationImage by viewModel.isShowNotificationImage.collectAsState(initial = true)
    val isClickPlayList by viewModel.isClickPlayList.collectAsState(initial = true)
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
    // 定时停止「自定义分钟」输入：对齐原版 TimeoutExitEditModal 的任意分钟数（移植版此前仅固定档位）。
    var customTimeoutMinutes by remember { mutableStateOf("") }

    // ============ 播放缓存（原版 player.cacheSize） ============
    // 当前缓存占用随播放增长，进入设置页与清理后各读一次即可（每次读都要遍历缓存索引，
    // 不做轮询），避免在滚动设置页时反复 IO。
    val playCacheKb by viewModel.playCacheSizeKb.collectAsState(initial = 1024)
    var showPlayCache by remember { mutableStateOf(false) }
    var playCacheUsedBytes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(showPlayCache) {
        if (showPlayCache) playCacheUsedBytes = com.lingxu.music.core.player.PlaybackCache.sizeBytes()
    }

    // ============ 歌词与链接缓存（对齐原版 MetaCache） ============
    // 条数在进入「其他」分区与清理后各读一次：LruCache 快照是 O(n) 拷贝，不做轮询。
    var showMetaCache by remember { mutableStateOf(false) }
    var metaLyricCount by remember { mutableIntStateOf(0) }
    var metaUrlCount by remember { mutableIntStateOf(0) }
    fun refreshMetaCounts() {
        metaLyricCount = com.lingxu.music.core.repository.MetaCache.lyricCount()
        metaUrlCount = com.lingxu.music.core.repository.MetaCache.urlCount()
    }
    LaunchedEffect(showMetaCache) { if (showMetaCache) refreshMetaCounts() }

    // initial 与落盘默认（SettingsRepository）保持一致，避免首帧闪出错误状态再跳变。
    val showLyricTranslation by viewModel.showLyricTranslation.collectAsState(initial = false)
    val showLyricRoma by viewModel.showLyricRoma.collectAsState(initial = false)
    val lyricAlign by viewModel.lyricAlign.collectAsState(initial = "left")
    val lyricFontSize by viewModel.lyricFontSize.collectAsState(initial = 1f)
    val isZoomActiveLrc by viewModel.isZoomActiveLrc.collectAsState(initial = false)
    val lyricFontShadow by viewModel.lyricFontShadow.collectAsState(initial = false)
    val showLyricProgress by viewModel.showLyricProgress.collectAsState(initial = false)
    // 桌面歌词（原版 desktopLyric.enable）：字号的本地值用于拖动时即时反馈
    val desktopLyricEnabled by viewModel.desktopLyricEnabled.collectAsState(initial = false)
    val desktopLyricLocked by viewModel.desktopLyricLocked.collectAsState(initial = false)
    val desktopLyricFontSize by viewModel.desktopLyricFontSize.collectAsState(initial = 18f)
    var localLyricFont by remember { mutableFloatStateOf(desktopLyricFontSize) }
    // 持久化字号是异步读出来的：用户尚未拖动前，跟随落盘值回填；
    // 一旦用户开始拖动就不再回灌（否则拖动过程中会被旧值拽回，与倍速/模糊滑块同一约定）。
    var lyricFontTouched by remember { mutableStateOf(false) }
    LaunchedEffect(desktopLyricFontSize) {
        if (!lyricFontTouched) localLyricFont = desktopLyricFontSize
    }
    // 桌面歌词原版样式项：单行 / 切换动画 / 不透明度 / 文字对齐
    val desktopLyricSingleLine by viewModel.desktopLyricSingleLine.collectAsState(initial = false)
    val desktopLyricToggleAnima by viewModel.desktopLyricToggleAnima.collectAsState(initial = true)
    val desktopLyricOpacity by viewModel.desktopLyricOpacity.collectAsState(initial = 100)
    val desktopLyricTextPositionX by viewModel.desktopLyricTextPositionX.collectAsState(initial = "center")
    val desktopLyricTextPositionY by viewModel.desktopLyricTextPositionY.collectAsState(initial = "top")
    var localLyricOpacity by remember { mutableIntStateOf(desktopLyricOpacity) }
    var lyricOpacityTouched by remember { mutableStateOf(false) }
    LaunchedEffect(desktopLyricOpacity) {
        if (!lyricOpacityTouched) localLyricOpacity = desktopLyricOpacity
    }
    // 悬浮歌词显示行数（原版 desktopLyric.maxLineNum）：本地值用于滑块即时反馈
    val desktopLyricMaxLineNum by viewModel.desktopLyricMaxLineNum.collectAsState(initial = 5)
    var localLyricMaxLineNum by remember { mutableIntStateOf(desktopLyricMaxLineNum) }
    var lyricMaxLineTouched by remember { mutableStateOf(false) }
    LaunchedEffect(desktopLyricMaxLineNum) {
        if (!lyricMaxLineTouched) localLyricMaxLineNum = desktopLyricMaxLineNum
    }
    // 悬浮歌词悬浮窗宽度（原版 desktopLyric.width，10~100，默认 100）
    val desktopLyricWidth by viewModel.desktopLyricWidth.collectAsState(initial = 100)
    var localLyricWidth by remember { mutableIntStateOf(desktopLyricWidth) }
    var lyricWidthTouched by remember { mutableStateOf(false) }
    LaunchedEffect(desktopLyricWidth) {
        if (!lyricWidthTouched) localLyricWidth = desktopLyricWidth
    }
    // 桌面歌词当前行颜色（原版 desktopLyric.style.lyricPlayedColor）：null = 跟随主题主色
    val desktopLyricPlayedColor by viewModel.desktopLyricPlayedColor.collectAsState(initial = null)
    // 桌面歌词后续行颜色（原版 desktopLyric.style.lyricUnplayColor）：null = 历史默认次要白
    val desktopLyricUnplayColor by viewModel.desktopLyricUnplayColor.collectAsState(initial = null)
    // 桌面歌词阴影颜色（原版 desktopLyric.style.lyricShadowColor）：null = 按文字亮度自动推导
    val desktopLyricShadowColor by viewModel.desktopLyricShadowColor.collectAsState(initial = null)

    val isShowHotSearch by viewModel.isShowHotSearch.collectAsState(initial = false)
    val isShowHistorySearch by viewModel.isShowHistorySearch.collectAsState(initial = false)

    val isShowSource by viewModel.isShowSource.collectAsState(initial = true)
    val isShowAlbumName by viewModel.isShowAlbumName.collectAsState(initial = false)
    val isShowInterval by viewModel.isShowInterval.collectAsState(initial = true)
    val saveScrollLocation by viewModel.saveScrollLocation.collectAsState(initial = true)
    val addMusicLocation by viewModel.addMusicLocation.collectAsState(initial = "top")

    val blurEnabled by viewModel.blurEnabled.collectAsState(initial = true)
    val blurRadius by viewModel.blurRadius.collectAsState(initial = 24f)
    // 拖动中的本地值：每帧写盘会重建 GlassContext 让整屏 backdrop 重画，落盘已节流
    // 同倍速/音量：不用 LaunchedEffect 回灌持久化值，避免拖动过程中滑块回弹
    var localBlur by remember { mutableFloatStateOf(blurRadius) }
    val userApiLog by viewModel.userApiLog.collectAsState(initial = false)
    // 应用内运行日志（对齐原版设置 → 其他 → 日志）：开关控制是否记录到 logcat，
    // 查看弹窗展示进程内的有界缓冲，音源与搜索出错时可在机内自查，无需连电脑看 logcat。
    var showRuntimeLog by remember { mutableStateOf(false) }
    var runtimeLogText by remember { mutableStateOf("") }

    // 离开设置页时提交倍速/音量节流跳过的最终值，避免拖完立刻返回导致调整丢失
    DisposableEffect(Unit) {
        onDispose {
            viewModel.commitPlaybackRate()
            viewModel.commitVolume()
            viewModel.commitDesktopLyricFontSize()
            viewModel.commitDesktopLyricOpacity()
            viewModel.commitDesktopLyricMaxLineNum()
            viewModel.commitDesktopLyricWidth()
        }
    }

    // 桌面歌词需要「显示在其他应用上层」权限：授权页是独立 Activity，返回时不重建本页，
    // 因此用窗口焦点变化来复检权限（无需引入 lifecycle-compose 依赖）。
    // 授权回来且用户此前点过开关时，自动把开关打开，避免「点了没反应」的观感。
    val overlayContext = androidx.compose.ui.platform.LocalContext.current
    var canDrawOverlay by remember { mutableStateOf(com.lingxu.music.core.system.DesktopLyric.canShow(overlayContext)) }
    var pendingLyricAuth by remember { mutableStateOf(false) }
    val overlayHostView = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(overlayHostView) {
        val listener = android.view.ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                canDrawOverlay = com.lingxu.music.core.system.DesktopLyric.canShow(overlayContext)
            }
        }
        overlayHostView.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            overlayHostView.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }
    LaunchedEffect(canDrawOverlay) {
        if (pendingLyricAuth && canDrawOverlay) {
            pendingLyricAuth = false
            viewModel.setDesktopLyricEnabled(true)
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
        // 主题主色（对齐原版「主题」皮肤）：只换一个主色，明暗与语义色由 Miuix 自动推导，
        // 深浅两套对比度保持一致；「默认」即升级前的外观。
        ArrowPreference(
            title = "主题色",
            summary = com.lingxu.music.ui.theme.themeColorLabel(themeColor) +
                if (themeMode == "monet") "（取色模式下不生效）" else "",
            onClick = { viewModel.cycleThemeColor() },
        )
        SwitchPreference(
            title = "播放页动态背景",
            summary = "播放页背景跟随当前封面主色，柔和渐变（对齐原版 theme.dynamicBg）",
            checked = dynamicBg,
            onCheckedChange = { viewModel.setDynamicBg(it) },
        )
        // 原版 theme.hideBgDark：深色模式下隐藏背景。移植版背景是封面主色渐变，
        // 与动态背景开关配套——仅在开启动态背景后才有意义，故同样用条件渲染。
        if (dynamicBg) {
            SwitchPreference(
                title = "深色模式隐藏背景",
                summary = "深色模式下不显示播放页渐变背景（对齐原版 theme.hideBgDark）",
                checked = hideBgDark,
                onCheckedChange = { viewModel.setHideBgDark(it) },
            )
        }
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
        // 字号实时预览（对齐原版 FontSize.tsx 的 SizeText）：固定基准字号用 sp，
        // LxTheme 已把 fontScale 注入 LocalDensity，切换档位时这段示例文字会随全站字号即时缩放，
        // 用户点档位当帧即可看到实际大小，不必退出设置页去猜。主色 + 次要色说明，纯展示不参与交互。
        Spacer(Modifier.size(8.dp))
        Text(
            "字体大小预览 Aa 聆序音乐",
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ArrowPreference(
            title = "分享方式",
            summary = when (shareType) {
                "clipboard" -> "复制到剪贴板"
                else -> "系统分享"
            },
            onClick = { viewModel.cycleShareType() },
        )
        // 对齐原版「歌曲来源名称」：切换内置音源显示原名（酷我音乐）或别名（小蜗音乐）。
        // 与主题/语言同一交互语言（点击循环），切换后全站音源名经 Compose State 即时刷新。
        ArrowPreference(
            title = "歌曲来源名称",
            summary = if (sourceNameType == "alias") "别名（小蜗音乐 等）" else "原名（酷我音乐 等）",
            onClick = { viewModel.cycleSourceNameType() },
        )
        SwitchPreference(
            title = "自动隐藏播放条",
            summary = "滚动列表时自动隐藏底部迷你播放条",
            checked = autoHidePlayBar,
            onCheckedChange = { viewModel.setAutoHidePlayBar(it) },
        )
        SwitchPreference(
            title = "进度条可拖动",
            summary = "在底部迷你播放条的进度条上拖动即可跳转播放位置（对齐原版）",
            checked = allowProgressBarSeek,
            onCheckedChange = { viewModel.setAllowProgressBarSeek(it) },
        )
        // 对齐原版 common.showBackBtn：二级页返回按钮显隐。默认开启保持移植版既有手感；
        // 关闭后返回手势/系统返回键仍可用，仅隐藏页面左上角图标（48dp 占位保留，标题不位移）。
        SwitchPreference(
            title = "显示返回按钮",
            summary = "二级页左上角显示返回图标（对齐原版 common.showBackBtn；关闭后仍可用系统返回）",
            checked = showBackBtn,
            onCheckedChange = { viewModel.setShowBackBtn(it) },
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
            title = "启动时打开播放详情页",
            summary = "有上次播放记录时，打开 App 直接进入播放页",
            checked = startupPushPlayDetail,
            onCheckedChange = { viewModel.setStartupPushPlayDetail(it) },
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
        SwitchPreference(
            title = "通知栏显示封面",
            summary = "媒体通知与锁屏界面显示专辑封面（对齐原版）",
            checked = isShowNotificationImage,
            onCheckedChange = { viewModel.setIsShowNotificationImage(it) },
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
            onClick = {
                customTimeoutMinutes = "" // 每次打开都从空输入开始，不残留上次值
                showTimeoutExit = true
            },
        )
        ArrowPreference(
            title = "播放缓存",
            summary = if (playCacheKb <= 0) {
                "不缓存：每次播放都从网络重新加载"
            } else {
                if (playCacheUsedBytes > 0) {
                    "已用 ${formatSize(playCacheUsedBytes)} / 上限 ${formatSize(playCacheKb.toLong() * 1024)}"
                } else {
                    "上限 ${formatSize(playCacheKb.toLong() * 1024)}，听过的歌会缓存在本地（点按调整）"
                }
            },
            onClick = { showPlayCache = true },
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
            // 对齐原版 playDetail.style.align 的 left/center/right 三档（此前摘要只覆盖两档）
            summary = when (lyricAlign) {
                "center" -> "居中"
                "right" -> "右对齐"
                else -> "左对齐"
            },
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
        SwitchPreference(
            title = "歌词字体阴影",
            summary = "给歌词文字加一层投影，浅色封面/壁纸下更清晰（对齐原版）",
            checked = lyricFontShadow,
            onCheckedChange = { viewModel.setLyricFontShadow(it) },
        )
        SwitchPreference(
            title = "歌词逐句定位线",
            summary = "手动滑动歌词时显示时间刻度线，点播放键跳到该句（对齐原版）",
            checked = showLyricProgress,
            onCheckedChange = { viewModel.setShowLyricProgress(it) },
        )
        // 桌面歌词（对应原版 desktopLyric.enable）：悬浮在其它应用之上显示当前歌词。
        // 未授权时先引导去系统页授权，返回后由窗口焦点复检自动打开开关。
        SwitchPreference(
            title = "桌面歌词",
            summary = when {
                desktopLyricEnabled && desktopLyricLocked -> "已开启：位置已锁定，轻点暂停 / 继续"
                desktopLyricEnabled -> "已开启：上下拖动移动位置，轻点暂停 / 继续"
                !canDrawOverlay -> "需要「显示在其他应用上层」权限，点按前往授权"
                else -> "在其它应用上方悬浮显示当前歌词"
            },
            checked = desktopLyricEnabled,
            onCheckedChange = { checked ->
                if (!checked) {
                    viewModel.setDesktopLyricEnabled(false)
                } else if (canDrawOverlay) {
                    viewModel.setDesktopLyricEnabled(true)
                } else {
                    pendingLyricAuth = true
                    runCatching {
                        overlayContext.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${overlayContext.packageName}"),
                            ),
                        )
                    }.onFailure { toast = "无法打开授权页，请在系统设置中手动授予悬浮窗权限" }
                }
            },
        )
        if (desktopLyricEnabled) {
            SliderPreference(
                title = "桌面歌词字号",
                summary = "${localLyricFont.toInt()}sp",
                value = localLyricFont,
                valueRange = 12f..28f,
                onValueChange = {
                    lyricFontTouched = true
                    localLyricFont = it
                    viewModel.setDesktopLyricFontSize(it)
                },
            )
            // 位置锁定（原版 desktopLyric.isLock）：锁定后不能拖动位置，防止误触移动，
            // 轻点暂停/继续不受影响。默认关，与原版一致。
            SwitchPreference(
                title = "锁定歌词位置",
                summary = if (desktopLyricLocked) "已锁定：拖动不会移动歌词" else "关闭：可上下拖动调整位置",
                checked = desktopLyricLocked,
                onCheckedChange = { viewModel.setDesktopLyricLocked(it) },
            )
            // 原版桌面歌词样式项（对齐 lx-music-mobile 的 LyricDesktop 设置组）。
            SwitchPreference(
                title = "单行显示",
                summary = if (desktopLyricSingleLine) "只显示当前歌词，不显示翻译副行" else "显示歌词与翻译副行",
                checked = desktopLyricSingleLine,
                onCheckedChange = { viewModel.setDesktopLyricSingleLine(it) },
            )
            // 悬浮歌词显示行数（原版 desktopLyric.maxLineNum，1~8，默认 5）：
            // 「单行显示」开启时此值不生效（强制只显示当前行），关闭时按此值显示当前行起的连续歌词。
            SliderPreference(
                title = "显示行数",
                summary = "${localLyricMaxLineNum} 行${if (desktopLyricSingleLine) "（单行显示已开启，暂不生效）" else ""}",
                value = localLyricMaxLineNum.toFloat(),
                valueRange = 1f..8f,
                onValueChange = {
                    lyricMaxLineTouched = true
                    val v = it.toInt().coerceIn(1, 8)
                    localLyricMaxLineNum = v
                    viewModel.setDesktopLyricMaxLineNum(v)
                },
            )
            // 悬浮窗宽度（原版 desktopLyric.width，10~100，默认 100）：
            // 100% 保留屏幕宽减内边距的历史观感，调小可让歌词不遮挡屏幕两侧内容。
            SliderPreference(
                title = "歌词宽度",
                summary = "$localLyricWidth%",
                value = localLyricWidth.toFloat(),
                valueRange = 10f..100f,
                onValueChange = {
                    lyricWidthTouched = true
                    val v = it.toInt().coerceIn(10, 100)
                    localLyricWidth = v
                    viewModel.setDesktopLyricWidth(v)
                },
            )
            SwitchPreference(
                title = "切换动画",
                summary = if (desktopLyricToggleAnima) "暂停 / 继续时淡入淡出" else "关闭：状态切换即时生效（省电、减弱动效）",
                checked = desktopLyricToggleAnima,
                onCheckedChange = { viewModel.setDesktopLyricToggleAnima(it) },
            )
            SliderPreference(
                title = "文字不透明度",
                summary = "$localLyricOpacity%",
                value = localLyricOpacity.toFloat(),
                valueRange = 10f..100f,
                onValueChange = {
                    lyricOpacityTouched = true
                    val v = it.toInt().coerceIn(10, 100)
                    localLyricOpacity = v
                    viewModel.setDesktopLyricOpacity(v)
                },
            )
            // 此处是「桌面歌词」的水平位置（left/center/right），与上方主歌词页的
            // ArrowPreference「歌词对齐」是两个不同设置。原标题同名易让用户误以为是同一项。
            SectionTitle("桌面歌词对齐")
            Spacer(Modifier.size(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("left" to "左", "center" to "居中", "right" to "右").forEach { (id, label) ->
                    FilterChip(
                        label = label,
                        selected = desktopLyricTextPositionX == id,
                        onClick = { viewModel.setDesktopLyricTextPositionX(id) },
                    )
                }
            }
            // 垂直对齐（原版 desktopLyric.textPosition.y，top/center/bottom）：与水平对齐组合。
            // 默认 top 与原版一致；悬浮 TextView 高度贴合内容，单行下与 center 观感无差。
            SectionTitle("垂直位置")
            Spacer(Modifier.size(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("top" to "顶部", "center" to "居中", "bottom" to "底部").forEach { (id, label) ->
                    FilterChip(
                        label = label,
                        selected = desktopLyricTextPositionY == id,
                        onClick = { viewModel.setDesktopLyricTextPositionY(id) },
                    )
                }
            }
            // 原版 LyricDesktop/Theme.tsx 的 9 色色板；额外「跟随主题」为移植版默认，
            // 保证老用户升级零突变。分成两行避免窄屏/大字号把胶囊挤成多行。
            SectionTitle("歌词颜色")
            Spacer(Modifier.size(4.dp))
            listOf(
                listOf<Pair<String?, String>>(null to "跟随主题", "#08E664" to "绿", "#FFFA12" to "黄", "#019CE4" to "蓝", "#FF1222" to "红"),
                listOf<Pair<String?, String>>("#EF6976" to "粉", "#C851D4" to "紫", "#FFA600" to "橙", "#000000" to "黑", "#FFFFFF" to "白"),
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { (hex, label) ->
                        FilterChip(
                            label = label,
                            selected = desktopLyricPlayedColor == hex,
                            onClick = { if (desktopLyricPlayedColor != hex) viewModel.setDesktopLyricPlayedColor(hex) },
                            horizontalPadding = 8,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.size(6.dp))
            }
            // 原版 LyricDesktop 另提供 lyricUnplayColor（后续行/未播放歌词颜色）。
            // null「默认」沿用历史次要白，保证老用户升级零突变；同样两行排布避免窄屏挤压。
            SectionTitle("未播放歌词颜色")
            Spacer(Modifier.size(4.dp))
            listOf(
                listOf<Pair<String?, String>>(null to "默认", "#FFFFFF" to "白", "#B3B3B3" to "浅灰", "#808080" to "灰", "#4D4D4D" to "深灰"),
                listOf<Pair<String?, String>>("#000000" to "黑", "#08E664" to "绿", "#019CE4" to "蓝", "#FF1222" to "红", "#C851D4" to "紫"),
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { (hex, label) ->
                        FilterChip(
                            label = label,
                            selected = desktopLyricUnplayColor == hex,
                            onClick = { if (desktopLyricUnplayColor != hex) viewModel.setDesktopLyricUnplayColor(hex) },
                            horizontalPadding = 8,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.size(6.dp))
            }
            // 桌面歌词阴影颜色（原版 desktopLyric.style.lyricShadowColor）：原版色板阴影仅两种
            // （黑字配白阴影、其余配 rgba(0,0,0,0.6) 黑阴影）。默认「自动」沿用按文字亮度推导的
            // 历史行为（老用户升级零突变）；显式选黑/白则覆盖，复用 FilterChip 的按压缩放、48dp
            // 命中区、读屏语义与 LocalReduceMotion 静帧降级，与上方两组颜色色板同一手感。
            SectionTitle("阴影颜色")
            Spacer(Modifier.size(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf<Pair<String?, String>>(
                    null to "自动",
                    "#000000" to "黑",
                    "#FFFFFF" to "白",
                ).forEach { (hex, label) ->
                    FilterChip(
                        label = label,
                        selected = desktopLyricShadowColor == hex,
                        onClick = { if (desktopLyricShadowColor != hex) viewModel.setDesktopLyricShadowColor(hex) },
                        horizontalPadding = 8,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

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
        // 对齐原版 list.isSaveScrollLocation：从二级页返回时保留列表滚动位置（默认开启）
        SwitchPreference(
            title = "记住列表位置",
            summary = "从歌单、榜单等详情页返回时，停在离开前的滚动位置",
            checked = saveScrollLocation,
            onCheckedChange = { viewModel.setSaveScrollLocation(it) },
        )
        // 对齐原版 list.isClickPlayList：点击列表里的歌曲时切到当前列表播放（仅歌单/榜单）
        SwitchPreference(
            title = "点歌切换到当前列表",
            summary = "点击歌单、榜单里的歌曲时，用整个列表作为播放队列；关闭则只播放单曲",
            checked = isClickPlayList,
            onCheckedChange = { viewModel.setIsClickPlayList(it) },
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
                toast = if (size > 0) "已清理 ${formatSize(size)} 缓存" else "没有可清理的缓存"
            },
        )
        ArrowPreference(
            title = "歌词与链接缓存",
            // 对齐原版「元数据缓存」：分别显示歌词/换源地址条数，可分别清理。
            // 移植版这两类缓存都在内存里，清理即时生效，无需重启。
            summary = "歌词 ${metaLyricCount} 条 · 播放链接 ${metaUrlCount} 条",
            onClick = { showMetaCache = true },
        )
        // 对齐原版 setting_other_log：查看/清空运行日志（音源脚本与内置 SDK 的 console）。
        // 副标题带当前条数，用户不看点进去就知道有没有内容；缓冲有上限、仅存内存。
        ArrowPreference(
            title = "运行日志",
            summary = "查看音源与搜索输出（最近 ${com.lingxu.music.core.system.AppLog.size()} 条，重启应用后清空）",
            onClick = {
                runtimeLogText = com.lingxu.music.core.system.AppLog.snapshot()
                showRuntimeLog = true
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

        // 开源项目信息（对齐原版 About.tsx：开源地址 / 最新版下载 / 常见问题）。
        // 原版「关于」页给出四个外链，移植版此前只有版本号与设备信息，用户无从查看项目出处。
        // 这里用一个入口汇总，弹窗内逐条给出，点击经系统浏览器打开（复用检查更新的 ACTION_VIEW 模式）。
        Spacer(Modifier.size(8.dp))
        ArrowPreference(
            title = "开源项目",
            summary = "项目地址、最新版下载与常见问题",
            onClick = { showAboutProject = true },
        )

        Spacer(Modifier.size(24.dp))
    }
    if (showAboutProject) {
        // 用一份 Link 列表驱动 UI，避免重复代码；每条独立 runCatching，单个链接失败不影响其它项。
        val aboutLinks = listOf(
            "开源地址" to "https://github.com/jty657/lx-music-compose",
            "原版项目" to "https://github.com/lyswhut/lx-music-mobile",
            "最新版下载" to "https://github.com/jty657/lx-music-compose/releases",
            "常见问题" to "https://github.com/lyswhut/lx-music-mobile/wiki",
        )
        val aboutCtx = androidx.compose.ui.platform.LocalContext.current
        GlassDialog(onDismissRequest = { showAboutProject = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("开源项目", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(
                    "聆序是洛雪音乐移动版的 Compose 移植项目，完全免费、开源。使用问题可查阅常见问题或到项目 Issue 反馈。",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(12.dp))
                aboutLinks.forEach { (label, url) ->
                    // 必须显式用 onClick= 命名参数：TextActionButton 的 onClick 不是末位参数
                    // （末位是 maxLines:Int），尾随 lambda 会被错误绑定到 maxLines 导致编译失败。
                    TextActionButton(
                        text = label,
                        onClick = {
                            runCatching {
                                aboutCtx.startActivity(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(url),
                                    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        },
                    )
                    Spacer(Modifier.size(4.dp))
                }
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { showAboutProject = false }) { Text("关闭") }
                }
            }
        }
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
                            // 规则为「歌名@歌手」，长度不可控：单行省略，避免长规则换行挤压右侧删除按钮。
                            Text(
                                rule,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
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
                            .dialogOptionClickable {
                                viewModel.setTimeoutExit(min)
                                showTimeoutExit = false
                            }
                            // 纯文字选项热区补到约 48dp（与搜索页/不喜欢列表同类项一致），字号不变
                            .padding(vertical = 14.dp),
                    )
                }
                // 对齐原版 TimeoutExitEditModal：允许输入任意分钟数，不再局限于固定档位。
                // 底层 setTimeoutExit(Int) 已支持任意分钟并做溢出钳制，这里只做输入解析与范围校验。
                Spacer(Modifier.size(8.dp))
                // 上限 1440 分钟（24 小时），下限 1；错误在输入框内联提示，不额外弹窗、不改动播放状态。
                val customMin = customTimeoutMinutes.trim().toIntOrNull()
                val customMinInvalid = customTimeoutMinutes.isNotEmpty() && (customMin == null || customMin !in 1..1440)
                androidx.compose.material3.OutlinedTextField(
                    value = customTimeoutMinutes,
                    onValueChange = { customTimeoutMinutes = it },
                    label = { Text("自定义分钟（1-1440）") },
                    singleLine = true,
                    isError = customMinInvalid,
                    supportingText = {
                        if (customMinInvalid) Text("请输入 1-1440 之间的分钟数")
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextActionButton(
                        text = "开始定时",
                        // 非法输入时按钮不响应，错误已由输入框内联提示；合法则落盘并关闭弹窗。
                        onClick = {
                            val min = customTimeoutMinutes.trim().toIntOrNull()
                            if (min != null && min in 1..1440) {
                                viewModel.setTimeoutExit(min)
                                showTimeoutExit = false
                            }
                        },
                    )
                }
                if (timeoutRemaining > 0) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "取消定时",
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .dialogOptionClickable {
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

    if (showPlayCache) {
        // 容量档位按 MB 选，不弹键盘输入：原版是数字输入框，但移动端弹一次键盘再校验
        // 反而比点选慢；档位覆盖 1MB~512MB，「关闭」单独作为 0（不缓存）。
        GlassDialog(onDismissRequest = { showPlayCache = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("播放缓存", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(4.dp))
                Text(
                    "已缓存 ${formatSize(playCacheUsedBytes)}" +
                        if (playCacheKb <= 0) "（当前已关闭）"
                        else " / 上限 ${formatSize(playCacheKb.toLong() * 1024)}",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        label = "关闭",
                        selected = playCacheKb <= 0,
                        onClick = {
                            viewModel.setPlayCacheSizeKb(0)
                            playCacheUsedBytes = 0L
                        },
                    )
                    FilterChip(
                        label = "1 MB",
                        selected = playCacheKb == 1024,
                        onClick = { viewModel.setPlayCacheSizeKb(1024) },
                    )
                    FilterChip(
                        label = "32 MB",
                        selected = playCacheKb == 32 * 1024,
                        onClick = { viewModel.setPlayCacheSizeKb(32 * 1024) },
                    )
                }
                Spacer(Modifier.size(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(64, 128, 256, 512).forEach { mb ->
                        FilterChip(
                            label = "$mb MB",
                            selected = playCacheKb == mb * 1024,
                            onClick = { viewModel.setPlayCacheSizeKb(mb * 1024) },
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    "听过的歌会写入本地，重复播放不再重新下载；关闭后每次播放都走网络。",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        val cleared = viewModel.clearPlayCache()
                        playCacheUsedBytes = 0L
                        toast = if (cleared > 0) "已清理 ${formatSize(cleared)} 播放缓存" else "没有可清理的播放缓存"
                        showPlayCache = false
                    }) { Text("清空缓存") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { showPlayCache = false }) { Text("完成") }
                }
            }
        }
    }

    if (showMetaCache) {
        GlassDialog(onDismissRequest = { showMetaCache = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("歌词与链接缓存", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(4.dp))
                Text(
                    "切歌/回上一首时直接复用，命中后不再重复取词与取址。清空后下次播放会重新获取。",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("歌词", modifier = Modifier.weight(1f))
                    Text(
                        "$metaLyricCount 条",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.size(12.dp))
                    Button(onClick = {
                        val n = com.lingxu.music.core.repository.MetaCache.clearLyric()
                        refreshMetaCounts()
                        toast = if (n > 0) "已清理 $n 条歌词缓存" else "没有可清理的歌词缓存"
                    }) { Text("清空") }
                }
                Spacer(Modifier.size(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("播放链接", modifier = Modifier.weight(1f))
                    Text(
                        "$metaUrlCount 条",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.size(12.dp))
                    Button(onClick = {
                        val n = com.lingxu.music.core.repository.MetaCache.clearUrl()
                        refreshMetaCounts()
                        toast = if (n > 0) "已清理 $n 条链接缓存" else "没有可清理的链接缓存"
                    }) { Text("清空") }
                }
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { showMetaCache = false }) { Text("完成") }
                }
            }
        }
    }

    // 运行日志查看/清空（对齐原版 ConfirmAlert：展示文本 + 清空 + 关闭）。
    // 打开时取一次快照：避免展示期间新日志持续写入导致文本跳动、难以阅读；
    // 清空后重新取快照并就地刷新副标题计数。仅动 alpha 的弹窗进场由 GlassDialog 统一负责。
    if (showRuntimeLog) {
        GlassDialog(onDismissRequest = { showRuntimeLog = false }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("运行日志", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(4.dp))
                Text(
                    "音源脚本与内置 SDK 的最近输出，用于排查导入/搜索失败。仅存于本次运行，重启后清空。",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        // 限定高度：长日志在弹窗内滚动，不撑破屏幕（斟酌过高度不触发布局抖动）
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (runtimeLogText.isBlank()) {
                        Text(
                            "暂无日志。使用音源或搜索后可在此查看输出。",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    } else {
                        Text(
                            runtimeLogText,
                            fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                        )
                    }
                }
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextActionButton(
                        text = "清空",
                        color = MiuixTheme.colorScheme.error,
                        onClick = {
                            com.lingxu.music.core.system.AppLog.clear()
                            runtimeLogText = com.lingxu.music.core.system.AppLog.snapshot()
                            toast = "已清空运行日志"
                        },
                    )
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = { showRuntimeLog = false }) { Text("完成") }
                }
            }
        }
    }
}

/** 字节数转可读大小（与设置页「清除缓存」提示同一口径，保留一位小数） */
private fun formatSize(bytes: Long): String = when {
    bytes <= 0L -> "0 B"
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    else -> String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
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
