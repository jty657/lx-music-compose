package com.lingxu.music.ui.screens.favorite

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.ui.components.GlassSurface
import androidx.compose.foundation.layout.Arrangement
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.core.model.SourceConst
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 我喜欢（收藏）页 */
@Composable
fun FavoriteScreen(viewModel: FavoriteViewModel = viewModel()) {
    val favorites by viewModel.favorites.collectAsState()
    val scope = rememberCoroutineScope()
    var addTarget by remember { mutableStateOf<MusicInfo?>(null) }
    val showSource by LxApp.app.settings.isShowSource.collectAsState(initial = true)
    val showInterval by LxApp.app.settings.isShowInterval.collectAsState(initial = true)
    val showAlbumName by LxApp.app.settings.isShowAlbumName.collectAsState(initial = false)

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        GlassTopBar(modifier = Modifier.padding(horizontal = -12.dp, vertical = -12.dp)) {
            Text(
                "我喜欢",
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
        Spacer(Modifier.size(8.dp))

        viewModel.errorMessage?.let { err ->
            Text(
                err,
                color = MiuixTheme.colorScheme.error,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            Spacer(Modifier.size(4.dp))
        }

        if (favorites.isEmpty()) {
            Text(
                "还没有收藏的歌曲",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            return@Column
        }

        Row {
            Button(onClick = { viewModel.playAll(favorites) }) { Text("播放全部") }
        }
        Spacer(Modifier.size(8.dp))

        LazyColumn {
            items(favorites, key = { it.id }) { music ->
                FavoriteCard(
                    music = music,
                    onPlay = {
                        scope.launch { viewModel.play(music) }
                    },
                    onRemove = {
                        scope.launch { viewModel.remove(music.id) }
                    },
                    onAddToList = { addTarget = music },
                    showSource = showSource,
                    showInterval = showInterval,
                    showAlbumName = showAlbumName,
                )
            }
        }
    }

    addTarget?.let { music ->
        var picked by remember(music.id) {
            mutableStateOf(LxApp.app.myListsRepository.lists.value.firstOrNull()?.id)
        }
        GlassDialog(onDismissRequest = { addTarget = null }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text("加入列表", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.size(12.dp))
                val lists = LxApp.app.myListsRepository.lists.collectAsState().value
                if (lists.isEmpty()) {
                    Text("还没有列表，请先到「我的列表」新建")
                } else {
                    Column {
                        lists.forEach { list ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { picked = list.id }
                                    .padding(vertical = 6.dp),
                            ) {
                                RadioButton(selected = picked == list.id, onClick = { picked = list.id })
                                Text(list.name, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { addTarget = null }) { Text("取消") }
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = {
                        picked?.let { viewModel.addToList(it, music) }
                        addTarget = null
                    }) { Text("加入") }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    music: MusicInfo,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onAddToList: () -> Unit = {},
    showSource: Boolean = true,
    showInterval: Boolean = true,
    showAlbumName: Boolean = false,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onPlay,
        onLongClick = onAddToList,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                val subtitle = buildList {
                    add(music.singer)
                    if (showAlbumName) music.meta.albumName.takeIf { it.isNotBlank() }?.let { add(it) }
                    if (showSource) add(SourceConst.displayName(music.source))
                    if (showInterval) music.interval?.let { add(it) }
                }.filter { it.isNotBlank() }
                Text(
                    subtitle.joinToString(" · "),
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Icon(
                MiuixIcons.Delete,
                contentDescription = "移除",
                modifier = Modifier
                    .size(24.dp)
                    .clickableNoIndication(onRemove),
            )
        }
    }
}

private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
            onClick = onClick,
        ),
    )
