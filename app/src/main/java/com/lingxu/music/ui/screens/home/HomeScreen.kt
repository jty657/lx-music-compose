package com.lingxu.music.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.core.model.Board
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 首页：音源切换入口 + 榜单 + 热搜 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val boards = viewModel.boards
    val hotSearch = viewModel.hotSearch
    val selectedSource = viewModel.selectedSource
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        // 音源入口
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = { navigator.push(Screen.Source) },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                IconWrap(MiuixIcons.Layers)
                Column(Modifier.padding(start = 12.dp)) {
                    Text("音源管理", fontSize = MiuixTheme.textStyles.main.fontSize)
                    Text(
                        viewModel.sourceLabel(),
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        // 我的列表 / 歌单广场入口
        Row(Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.weight(1f),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = { navigator.push(Screen.MyLists) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    IconWrap(MiuixIcons.Layers)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("我的列表", fontSize = MiuixTheme.textStyles.main.fontSize)
                        Text(
                            "本地歌单与收藏",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
            Card(
                modifier = Modifier.weight(1f),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = { navigator.push(Screen.SongList(selectedSource)) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    IconWrap(MiuixIcons.Layers)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("歌单广场", fontSize = MiuixTheme.textStyles.main.fontSize)
                        Text(
                            "平台歌单",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.size(12.dp))

        // 热搜（可在设置里关闭）
        val showHotSearch by LxApp.app.settings.isShowHotSearch.collectAsState(initial = true)
        if (showHotSearch && hotSearch.isNotEmpty()) {
            Text("热搜", fontSize = MiuixTheme.textStyles.title1.fontSize)
            Spacer(Modifier.size(4.dp))
            val hotState = rememberLazyListState()
            com.lingxu.music.ui.components.ReportScrollForMiniBar(hotState)
            LazyColumn(Modifier.weight(1f), state = hotState) {
                itemsIndexed(hotSearch) { index, word ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                    ) {
                        Text(
                            "${index + 1}",
                            color = MiuixTheme.colorScheme.primary,
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                        )
                        Text(
                            word,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = MiuixTheme.textStyles.body1.fontSize,
                        )
                    }
                }
            }
        } else {
            // 榜单
            Text("排行榜", fontSize = MiuixTheme.textStyles.title1.fontSize)
            Spacer(Modifier.size(4.dp))
            val boardState = rememberLazyListState()
            com.lingxu.music.ui.components.ReportScrollForMiniBar(boardState)
            LazyColumn(Modifier.weight(1f), state = boardState) {
                items(boards, key = { it.id }) { board ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        pressFeedbackType = PressFeedbackType.Sink,
                        onClick = {
                            val (source, bangId, name) = viewModel.boardTarget(board)
                            navigator.push(Screen.BoardList(source, bangId, name))
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            IconWrap(MiuixIcons.Music)
                            Text(board.name, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconWrap(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    top.yukonga.miuix.kmp.basic.Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
}
