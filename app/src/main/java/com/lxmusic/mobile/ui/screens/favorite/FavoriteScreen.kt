package com.lxmusic.mobile.ui.screens.favorite

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lxmusic.mobile.LxApp
import com.lxmusic.mobile.core.model.MusicInfo
import kotlinx.coroutines.launch
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

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Text("我喜欢", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(8.dp))

        if (favorites.isEmpty()) {
            Text(
                "还没有收藏的歌曲",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
            return@Column
        }

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
                )
            }
        }
    }
}

@Composable
private fun FavoriteCard(music: MusicInfo, onPlay: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onPlay,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(MiuixIcons.Play, contentDescription = "播放", modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(music.name, fontSize = MiuixTheme.textStyles.main.fontSize)
                Text(
                    "${music.singer} · ${music.interval ?: ""}",
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
