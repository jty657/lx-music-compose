package com.lingxu.music.ui.screens.source

import com.lingxu.music.ui.components.BackIconButton
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    var deletingId by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
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

        Row(Modifier.fillMaxWidth()) {
            Button(onClick = { showUrlInput = true }) {
                Text("网络导入")
            }
            Spacer(Modifier.size(8.dp))
            Button(onClick = { filePicker.launch(arrayOf("text/*", "application/javascript")) }) {
                Text("文件导入")
            }
            Spacer(Modifier.size(8.dp))
            // 剪贴板导入：对齐手机版「复制音源链接或脚本文本后一键导入」的习惯
            Button(onClick = { viewModel.importFromClipboard() }) {
                Text("剪贴板导入")
            }
        }

        Spacer(Modifier.size(8.dp))

        Text("内置音源", fontSize = MiuixTheme.textStyles.body2.fontSize)
        Spacer(Modifier.size(4.dp))

        SourceConst.ONLINE.forEach { source ->
            GlassSurface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { viewModel.switchSource(source) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Text(
                        SourceConst.displayName(source),
                        modifier = Modifier.weight(1f),
                    )
                    if (currentSource == source) {
                        Text("✓ 使用中", color = MiuixTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("自定义音源（${userApis.size}/20）", fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.weight(1f))
            // 删除入口是长按，缺少发现性提示，用户往往不知道如何删除已导入音源
            Text(
                "长按音源可删除",
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.switchSource(api.id) },
                            onLongClick = {
                                deletingId = api.id
                            },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    // 脚本名来自用户导入、完全不可控：补单行省略，避免长名把右侧
                                    // 「✓ 使用中」挤出屏幕（与首页榜单/歌单项同一省略语言）。
                                    Text(api.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${api.author} ${api.version}".trim(),
                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
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
                    Button(onClick = {
                        showUrlInput = false
                        viewModel.importFromUrl(importUrl)
                        importUrl = ""
                    }) { Text("导入") }
                    }
                }
            }
    }

    // 删除确认
    deletingId?.let { id ->
        GlassDialog(onDismissRequest = { deletingId = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("删除音源", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                Text("确定删除该自定义音源吗？")
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { deletingId = null }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        viewModel.delete(id)
                        deletingId = null
                    }) { Text("删除") }
                }
            }
        }
    }
}
