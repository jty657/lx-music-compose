package com.lxmusic.mobile.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.MusicInfo
import com.lxmusic.mobile.core.model.SourceConst
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import com.lxmusic.mobile.ui.navigation.Screen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val textFieldState = remember { TextFieldState() }
    val sources = listOf("all") + SourceConst.ONLINE
    var selectedSource by remember { mutableStateOf("all") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        // 搜索输入框
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                state = textFieldState,
                label = "搜索歌曲、歌手",
                useLabelAsPlaceholder = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = {
                    val text = textFieldState.text.toString()
                    if (text.isNotBlank()) {
                        scope.launch {
                            viewModel.search(text, selectedSource)
                        }
                    }
                },
            ) {
                Icon(MiuixIcons.Search, contentDescription = "搜索")
            }
        }

        Spacer(Modifier.height(8.dp))

        // 音源选择
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            sources.forEach { source ->
                Button(
                    onClick = { selectedSource = source },
                    colors = if (source == selectedSource) null else null,
                ) {
                    Text(
                        text = when (source) {
                            "all" -> "聚合"
                            "kw" -> "酷我"
                            "kg" -> "酷狗"
                            "tx" -> "QQ音乐"
                            "wy" -> "网易"
                            "mg" -> "咪咕"
                            else -> source
                        },
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 结果列表
        val results by viewModel.results
        LazyColumn {
            items(results, key = { it.id }) { music ->
                SearchResultCard(
                    music = music,
                    onClick = {
                        scope.launch {
                            viewModel.play(music)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(music: MusicInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(32.dp))
            Column(Modifier.padding(start = 12.dp)) {
                Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                Text(
                    "${music.singer} · ${music.interval ?: ""}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
