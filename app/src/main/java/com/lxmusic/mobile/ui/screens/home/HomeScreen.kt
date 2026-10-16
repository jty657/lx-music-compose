package com.lxmusic.mobile.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.core.model.Board
import com.lxmusic.mobile.core.model.SourceConst
import com.lxmusic.mobile.ui.navigation.LocalNavigator
import com.lxmusic.mobile.ui.navigation.Screen
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
    val boards by viewModel.boards.collectAsState()
    val hotSearch by viewModel.hotSearch.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

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
                        color = MiuixTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.size(12.dp))

        // 榜单
        Text("排行榜", fontSize = MiuixTheme.textStyles.title.fontSize)
        Spacer(Modifier.size(4.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(boards, key = { it.id }) { board ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = {
                        viewModel.loadBoard(board)
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

@Composable
private fun IconWrap(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    top.yukonga.miuix.kmp.basic.Icon(icon, modifier = Modifier.size(28.dp))
}
