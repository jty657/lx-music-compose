package com.lxmusic.mobile.ui.screens.source

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.ApiSourceInfo
import com.lxmusic.mobile.core.model.SourceConst
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 音源管理页：内置音源 + 自定义音源，支持网络导入与文件导入 */
@Composable
fun SourceScreen(viewModel: SourceViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val userApis by viewModel.userApis.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var importUrl by remember { mutableStateOf("") }
    var showUrlInput by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("音源管理", fontSize = MiuixTheme.textStyles.title.fontSize)

        Spacer(Modifier.size(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(onClick = { showUrlInput = true }) {
                Text("网络导入")
            }
            Spacer(Modifier.size(8.dp))
            Button(onClick = { filePicker.launch(arrayOf("text/*", "application/javascript", "*/*")) }) {
                Text("文件导入")
            }
        }

        Spacer(Modifier.size(8.dp))

        Text("内置音源", fontSize = MiuixTheme.textStyles.body2.fontSize)
        Spacer(Modifier.size(4.dp))

        SourceConst.ONLINE.forEach { source ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = {
                    viewModel.switchSource(source)
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Text(
                        when (source) {
                            "kw" -> "酷我音乐"
                            "kg" -> "酷狗音乐"
                            "tx" -> "QQ音乐"
                            "wy" -> "网易音乐"
                            "mg" -> "咪咕音乐"
                            else -> source
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (currentSource == source) {
                        Text("✓ 使用中", color = MiuixTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Text("自定义音源（${userApis.size}/20）", fontSize = MiuixTheme.textStyles.body2.fontSize)
        Spacer(Modifier.size(4.dp))

        LazyColumn {
            items(userApis, key = { it.id }) { api ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = {
                        viewModel.switchSource(api.id)
                    },
                    onLongPress = {
                        deletingId = api.id
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(api.name)
                            Text(
                                "${api.author} ${api.version}".trim(),
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (currentSource == api.id) {
                            Text("✓ 使用中", color = MiuixTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // 状态提示
        viewModel.statusMessage?.let { msg ->
            Text(msg, color = MiuixTheme.colorScheme.error, fontSize = MiuixTheme.textStyles.body2.fontSize)
        }
    }

    // 网络导入对话框
    if (showUrlInput) {
        AlertDialog(
            onDismissRequest = { showUrlInput = false },
            title = { Text("网络导入音源") },
            text = {
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
            },
            confirmButton = {
                Button(onClick = {
                    showUrlInput = false
                    viewModel.importFromUrl(importUrl)
                    importUrl = ""
                }) { Text("导入") }
            },
            dismissButton = {
                Button(onClick = { showUrlInput = false }) { Text("取消") }
            },
        )
    }

    // 删除确认
    deletingId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("删除音源") },
            text = { Text("确定删除该自定义音源吗？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(id)
                    deletingId = null
                }) { Text("删除") }
            },
            dismissButton = {
                Button(onClick = { deletingId = null }) { Text("取消") }
            },
        )
    }
}
