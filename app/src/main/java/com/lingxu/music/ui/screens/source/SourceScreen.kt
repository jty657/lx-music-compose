package com.lingxu.music.ui.screens.source

import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.TextActionButton
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.animateListItem
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import com.lingxu.music.ui.navigation.LocalNavigator

/** 音源管理页：内置音源 + 自定义音源，支持网络导入与文件导入 */
@Composable
fun SourceScreen(viewModel: SourceViewModel = viewModel()) {
    val userApis by viewModel.userApis.collectAsState()
    val currentSource = viewModel.currentSource

    val navigator = LocalNavigator.current
    var importUrl by remember { mutableStateOf("") }
    var showUrlInput by remember { mutableStateOf(false) }
    // 长按音源打开「管理音源」对话框（导出/删除）的目标 id
    var managingId by remember { mutableStateOf<String?>(null) }
    // 待导出的音源 id：点击「导出脚本」时先记下，SAF 选好保存位置后再写文件
    var exportingId by remember { mutableStateOf<String?>(null) }
    // 待删除音源 id：对齐原版 user_api_remove_tip 的二次确认，删除不可撤销，误触代价高。
    // 点「删除」只记下 id 并弹出确认框，确认后才真正调用 delete（删除逻辑本身不变）。
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
    }

    // 导出脚本文本到用户选择的位置（对齐原版 ScriptImportExport 的导出方向）。
    // CreateDocument 是 AndroidX Activity 契约，项目已依赖 activity-compose，无需新增依赖。
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/javascript"),
    ) { uri: Uri? ->
        if (uri != null) {
            exportingId?.let { viewModel.exportScript(it, uri) }
        }
        exportingId = null
    }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.PageH, vertical = 10.dp),
            ) {
                // 从首页卡片 push 进来时是二级页，需要可见的返回入口
                if (navigator.backStack.size > 1) {
                    BackIconButton(onClick = { navigator.pop() })
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    "音源管理",
                    fontSize = MiuixTheme.textStyles.title1.fontSize,
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                // 大屏宽度上限：内容不再被拉伸到 1000dp+，手机端完全无变化
                .widthIn(max = ContentMaxWidth)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))

        // 三个导入按钮改横向可滚动：系统大字号或窄屏下，固定 Row 会把「剪贴板导入」挤出屏幕，
        // 与搜索页音源 chip、多选底栏一致改 LazyRow，按钮既不压缩也不裁切，超出部分可横滑。
        LazyRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Button(onClick = { showUrlInput = true }) {
                    Text("网络导入")
                }
            }
            item {
                Button(onClick = { filePicker.launch(arrayOf("text/*", "application/javascript")) }) {
                    Text("文件导入")
                }
            }
            // 剪贴板导入：对齐手机版「复制音源链接或脚本文本后一键导入」的习惯
            item {
                Button(onClick = { viewModel.importFromClipboard() }) {
                    Text("剪贴板导入")
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        // 音源说明与 FAQ 入口（对齐原版 UserApiEditModal 底部的 user_api_readme + FAQ + user_api_note）。
        // 新用户往往不知道自定义音源脚本从哪来，这里给一句话说明与可点的编写文档入口；
        // 用 TextActionButton 复用全站统一热区/按压反馈，Intent 打开外部浏览器沿用设置页既有写法。
        val faqContext = LocalContext.current
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "音源脚本编写说明：",
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                // 与右侧 FAQ 按钮同处一行：大字号下允许前缀收缩省略，避免把按钮挤出屏幕
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            TextActionButton(
                text = "FAQ",
                color = MiuixTheme.colorScheme.primary,
                onClick = {
                    runCatching {
                        faqContext.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://lyswhut.github.io/lx-music-doc/mobile/custom-source"),
                            ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }

        Spacer(Modifier.size(8.dp))

        Text("内置音源", fontSize = MiuixTheme.textStyles.body2.fontSize)
        Spacer(Modifier.size(4.dp))

        SourceConst.ONLINE.forEach { source ->
            // 当前启用音源整卡主色淡染，与播放队列「正在播放」、搜索音源 chip 的选中态同一设计语言。
            // GlassSurface 内部对 tint 走 animateColorAsState，切换音源时底色平滑淡入淡出（静帧降级已内置）。
            // 仅视觉反馈，switchSource 的切换逻辑完全不变。
            val inUse = currentSource == source
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    // 读屏播报「按钮 + 已选中/未选中」：此前选中态只有文字「✓ 使用中」，
                    // TalkBack 用户无法在切换前得知哪一路音源正在使用。
                    .semantics { selected = inUse },
                tint = if (inUse) MiuixTheme.colorScheme.primary.copy(alpha = 0.10f) else null,
                onClick = { viewModel.switchSource(source) },
                role = Role.Button,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Text(
                        SourceConst.displayName(source),
                        modifier = Modifier.weight(1f),
                        color = if (inUse) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                    )
                    if (inUse) {
                        Text("✓ 使用中", color = MiuixTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("自定义音源（${userApis.size}/20）", fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.weight(1f))
            // 管理入口（导出/删除）是长按，缺少发现性提示，用户往往不知道长按可导出已导入音源。
            // 大字号下补单行省略，避免提示换行把计数行撑高。
            Text(
                "长按音源可导出或删除",
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(4.dp))

        if (userApis.isEmpty()) {
            // 空态：首次进入、零导入时不再是一整块空白（全站其它列表页均有空态）
            EmptyHint(icon = MiuixIcons.File, text = "还没有自定义音源，点上方按钮导入")
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(userApis, key = { it.id }) { api ->
                    // 动画/间距挂在外层 Column：卡片与其下方的「更新提示」设置行作为一个整体让位。
                    Column(
                        modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                    ) {
                        // 与内置音源一致的选中态主色淡染：当前启用的自定义音源整卡淡染，
                        // 长列表中扫视定位更快；切换时由 GlassSurface 的 tint 过渡平滑淡变。
                        val inUse = currentSource == api.id
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            tint = if (inUse) MiuixTheme.colorScheme.primary.copy(alpha = 0.10f) else null,
                            onClick = { viewModel.switchSource(api.id) },
                            onLongClick = {
                                managingId = api.id
                            },
                            // 这里的长按是「管理音源」（导出/删除）而非歌曲操作，读屏必须如实播报，
                            // 否则 TalkBack 用户会听到错误的「长按，打开歌曲操作」。
                            onLongClickLabel = "管理音源",
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    // 脚本名来自用户导入、完全不可控：补单行省略，避免长名把右侧
                                    // 「✓ 使用中」挤出屏幕（与首页榜单/歌单项同一省略语言）。
                                    Text(
                                        api.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (inUse) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                                    )
                                    // 对齐原版 List.tsx：版本号纯数字时补 v 前缀（formatVersionName），
                                    // 并把脚本头部的 @description 单独一行展示。description 早已由
                                    // ApiSourceRepository.matchInfo 解析入库，但 UI 此前只显示作者+版本，
                                    // 用户在音源列表里看不到该脚本的用途说明。
                                    Text(
                                        listOfNotNull(
                                            api.version.takeIf { it.isNotBlank() }?.let { formatVersionName(it) },
                                            api.author.takeIf { it.isNotBlank() },
                                        ).joinToString("  "),
                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (api.description.isNotBlank()) {
                                        Text(
                                            api.description,
                                            fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            // 描述至多两行：脚本头部注释已限制 36 字符 + "...",
                                            // 两行足够完整展示，超长仍省略以免撑高列表行。
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                if (currentSource == api.id) {
                                    Text("✓ 使用中", color = MiuixTheme.colorScheme.primary)
                                }
                            }
                        }
                        // 「更新提示」开关放在卡片外侧：若嵌在卡片内，点击开关会冒泡触发卡片的
                        // 切换音源 onClick（Compose 父子 clickable 会同帧响应），故独立成行。
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "更新提示",
                                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            Spacer(Modifier.size(4.dp))
                            TextActionButton(
                                text = if (api.allowShowUpdateAlert) "开" else "关",
                                color = if (api.allowShowUpdateAlert) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                onClick = {
                                    viewModel.setAllowShowUpdateAlert(api.id, !api.allowShowUpdateAlert)
                                },
                            )
                        }
                    }
                }
            }
        }

        // 状态提示（成功/进行中用次要色，只有失败才是红字）
        viewModel.statusMessage?.let { msg ->
            // 导入失败的长异常信息限制两行并省略，避免撑高底部、压缩上方音源列表。
            Text(
                msg,
                color = if (viewModel.statusIsError) MiuixTheme.colorScheme.error
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        }
    }

    // 网络导入对话框
    if (showUrlInput) {
        GlassDialog(
                onDismissRequest = {
                showUrlInput = false
                importUrl = ""
            },
            ) {
                Column(Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        "网络导入音源",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("输入音源脚本 URL（http/https）", fontSize = MiuixTheme.textStyles.body2.fontSize)
                        Spacer(Modifier.size(8.dp))
                        OutlinedTextField(
                            value = importUrl,
                            onValueChange = { importUrl = it },
                            placeholder = { Text("https://example.com/source.js") },
                            singleLine = true,
                        )
                    }
                    Spacer(Modifier.size(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        showUrlInput = false
                        importUrl = ""
                    }) { Text("取消") }
                        Spacer(Modifier.size(8.dp))
                    Button(
                        enabled = !viewModel.importing,
                        onClick = {
                            showUrlInput = false
                            viewModel.importFromUrl(importUrl)
                            importUrl = ""
                        },
                    ) { Text(if (viewModel.importing) "导入中…" else "导入") }
                    }
                }
            }
    }

    // 管理音源（导出 / 删除）
    managingId?.let { id ->
        // 对齐原版 user_api_remove_tip（文案带 {name}）：让用户在操作时看清针对的是哪个音源，
        // 避免多个脚本时误删；从已收集的 userApis 按 id 取 name，找不到时回退到泛化文案。
        val managingName = userApis.find { it.id == id }?.name
        GlassDialog(onDismissRequest = { managingId = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("管理音源", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(
                    if (managingName.isNullOrBlank()) "选择对该自定义音源的操作"
                    else "音源「$managingName」",
                )
                Spacer(Modifier.size(16.dp))
                // 三按钮改为横向可滚动：窄屏 + 大字号下「删除」不再被裁掉。
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = { managingId = null }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    // 导出脚本（原版 ScriptImportExport 的导出方向）：SAF 选择保存位置后写入，
                    // 未选位置（取消）时 exportLauncher 回调 uri=null，不会触发写入。
                    Button(onClick = {
                        exportingId = id
                        managingId = null
                        exportLauncher.launch("${managingName ?: "lx-music-source"}.js")
                    }) { Text("导出脚本") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        // 对齐原版 handleRemove：删除前先确认（带音源名），确认后才真正移除。
                        pendingDeleteId = id
                        managingId = null
                    }) { Text("删除") }
                }
            }
        }
    }

    // 删除二次确认（对齐原版 UserApiEditModal/List.tsx 的 confirmDialog + user_api_remove_tip）。
    // 文案带音源名，用户在多个脚本中能看清针对的是哪一个，避免误删；取消保持原状。
    pendingDeleteId?.let { id ->
        val deleteName = userApis.find { it.id == id }?.name
        GlassDialog(onDismissRequest = { pendingDeleteId = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("删除音源", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text(
                    if (deleteName.isNullOrBlank()) "你真的要移除这个音源吗？"
                    else "你真的要移除「$deleteName」吗？",
                )
                Spacer(Modifier.size(16.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = { pendingDeleteId = null }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        viewModel.delete(id)
                        pendingDeleteId = null
                    }) { Text("删除") }
                }
            }
        }
    }
}

/**
 * 对齐原版 formatVersionName：版本号以数字开头时补 v 前缀（如 1.2.3 → v1.2.3），
 * 已是 v 开头或非数字开头（如日期型）则原样显示。空串由调用方 takeIf 过滤，不进这里。
 */
private fun formatVersionName(version: String): String =
    if (version.firstOrNull()?.isDigit() == true) "v$version" else version
