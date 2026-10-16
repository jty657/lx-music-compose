package com.lingxu.music.ui.screens.player

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.player.PlayMode
import com.lingxu.music.core.request.HttpClient
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 播放页：封面、歌词、控制按钮 */
@Composable
fun PlayerScreen(viewModel: PlayerViewModel = viewModel()) {
    val state by LxApp.app.playerRepository.state.collectAsState()
    val navigator = LocalNavigator.current
    val lyricFontScale by LxApp.app.settings.lyricFontSize.collectAsState(initial = 1f)
    val zoomActiveLrc by LxApp.app.settings.isZoomActiveLrc.collectAsState(initial = false)
    val showLrcTranslation by LxApp.app.settings.showLyricTranslation.collectAsState(initial = true)
    val showLrcRoma by LxApp.app.settings.showLyricRoma.collectAsState(initial = false)
    val lyricAlign by LxApp.app.settings.lyricAlign.collectAsState(initial = "left")

    // 拖动进度条时，用本地值展示，避免被 500ms 的位置更新覆盖导致回弹
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1L).toFloat()
    val shownPosition = dragValue ?: state.positionMs.toFloat()

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("正在播放", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(12.dp))

        // 歌曲信息
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    state.current?.name ?: "未在播放",
                    fontSize = MiuixTheme.textStyles.main.fontSize,
                )
                Text(
                    state.current?.singer ?: "",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        // 封面
        state.coverUrl?.let { url ->
            AlbumCover(url, Modifier.fillMaxWidth(0.7f).aspectRatio(1f))
            Spacer(Modifier.size(12.dp))
        }

        // 歌词（滚动到当前行）
        LyricView(
            state,
            Modifier.weight(1f),
            fontScale = lyricFontScale,
            zoomActive = zoomActiveLrc,
            showTranslation = showLrcTranslation,
            showRoma = showLrcRoma,
            centered = lyricAlign == "center",
        )

        // 进度条
        Slider(
            value = shownPosition,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let { viewModel.seekTo(it.toLong()) }
                dragValue = null
            },
            valueRange = 0f..duration,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(viewModel.formatTime(shownPosition.toLong()), fontSize = MiuixTheme.textStyles.body2.fontSize)
            Spacer(Modifier.weight(1f))
            Text(viewModel.formatTime(state.durationMs), fontSize = MiuixTheme.textStyles.body2.fontSize)
        }

        state.error?.let { err ->
            Text(
                err,
                color = MiuixTheme.colorScheme.error,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
            )
        }

        // 控制按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            IconButtonWrap(MiuixIcons.Back, onClick = { viewModel.prev() })
            Spacer(Modifier.size(16.dp))
            IconButtonWrap(
                if (state.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                onClick = { viewModel.togglePlay() },
            )
            Spacer(Modifier.size(16.dp))
            IconButtonWrap(MiuixIcons.Forward, onClick = { viewModel.next() })
            Spacer(Modifier.size(16.dp))
            IconButtonWrap(
                MiuixIcons.Layers,
                onClick = { navigator.push(Screen.PlayQueue) },
            )
        }

        // 播放模式
        Row {
            PlayMode.ALL.forEach { mode ->
                Text(
                    PlayMode.label(mode),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.setMode(mode) },
                    color = if (state.playMode == mode) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }
        }
    }
}

/** 歌词视图：显示当前行并滚动跟随 */
@Composable
private fun LyricView(
    state: com.lingxu.music.core.player.PlayState,
    modifier: Modifier = Modifier,
    fontScale: Float = 1f,
    zoomActive: Boolean = false,
    showTranslation: Boolean = true,
    showRoma: Boolean = false,
    centered: Boolean = false,
) {
    if (state.lyricLines.isEmpty()) {
        Text(
            "暂无歌词",
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = modifier,
        )
        return
    }
    val scrollState = rememberScrollState()
    val currentIndex = state.currentLyricIndex
    val baseSp = (MiuixTheme.textStyles.body2.fontSize.value * fontScale).sp
    androidx.compose.runtime.LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            // 让当前行大致居中
            scrollState.animateScrollTo(currentIndex * 40)
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        state.lyricLines.forEachIndexed { index, line ->
            Column(
                horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    line.text,
                    fontSize = if (zoomActive && index == currentIndex) baseSp * 1.4f else baseSp,
                    color = if (index == currentIndex) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (showRoma) {
                    line.roma?.let { roma ->
                        Text(
                            roma,
                            fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
                if (showTranslation) {
                    line.translation?.let { tr ->
                        Text(
                            tr,
                            fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 专辑封面：用 OkHttp 拉取并缓存 Bitmap（不引入图片加载库）。
 * 失败时显示占位色块。
 */
private val coverCache = java.util.concurrent.ConcurrentHashMap<String, android.graphics.Bitmap?>()

@Composable
private fun AlbumCover(url: String, modifier: Modifier = Modifier) {
    var bitmap by remember { mutableStateOf(coverCache[url]) }

    androidx.compose.runtime.LaunchedEffect(url) {
        if (coverCache.containsKey(url)) {
            bitmap = coverCache[url]
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = HttpClient.instance.fetchSync(url).body as? ByteArray
                bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            }.getOrNull()
        }
        coverCache[url] = loaded
        bitmap = loaded
    }

    Card(modifier = modifier) {
        val bm = bitmap
        if (bm != null) {
            Image(
                bitmap = bm.asImageBitmap(),
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun IconButtonWrap(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            ),
    )
}
