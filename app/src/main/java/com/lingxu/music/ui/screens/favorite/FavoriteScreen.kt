package com.lingxu.music.ui.screens.favorite

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.dialogOptionClickable
import androidx.compose.foundation.layout.Arrangement
import com.lingxu.music.ui.components.GlassDialog
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 我喜欢（收藏）页 */
@Composable
fun FavoriteScreen(viewModel: FavoriteViewModel = viewModel()) {
    val favorites by viewModel.favorites.collectAsState()
    val scope = rememberCoroutineScope()
    var addTarget by remember { mutableStateOf<MusicInfo?>(null) }
    // 长按歌曲弹出的操作菜单目标（null = 未打开）
    var actionTarget by remember { mutableStateOf<MusicInfo?>(null) }
    val showSource by LxApp.app.settings.isShowSource.collectAsState(initial = true)
    val showInterval by LxApp.app.settings.isShowInterval.collectAsState(initial = true)
    val showAlbumName by LxApp.app.settings.isShowAlbumName.collectAsState(initial = false)

    Column(
        Modifier
            .fillMaxSize(),
    ) {
        GlassTopBar {
            Text(
                "我喜欢",
                fontSize = MiuixTheme.textStyles.title1.fontSize,
                modifier = Modifier.padding(horizontal = Dimens.PageH, vertical = 10.dp),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                // 大屏宽度上限：内容不再被拉伸到 1000dp+，手机端完全无变化
                .widthIn(max = ContentMaxWidth)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.size(8.dp))

        if (favorites.isEmpty()) {
            EmptyHint(icon = MiuixIcons.Favorites, text = "还没有收藏的歌曲")
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { viewModel.playAll(favorites) }) { Text("播放全部") }
            Spacer(Modifier.weight(1f))
            // 长按加入列表此前无任何提示，用户无从发现（与音源页「长按音源可删除」同类）
            Text(
                "长按歌曲可加入列表等更多操作",
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Spacer(Modifier.size(8.dp))

        // 列表滚动上报，供迷你播放条自动隐藏（设置项 common.autoHidePlayBar）
        val listState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(listState)
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(favorites, key = { it.id }) { music ->
                FavoriteCard(
                    modifier = animateListItem(),
                    music = music,
                    onPlay = {
                        scope.launch { viewModel.play(music) }
                    },
                    onRemove = {
                        scope.launch { viewModel.remove(music.id) }
                    },
                    onAddToList = { actionTarget = music },
                    showSource = showSource,
                    showInterval = showInterval,
                    showAlbumName = showAlbumName,
                )
            }
        }
        }
    }

    // 长按弹出的歌曲操作菜单（播放走与点击同一条 viewModel.play）
    actionTarget?.let { music ->
        com.lingxu.music.ui.components.SongActionMenu(
            music = music,
            onPlay = { scope.launch { viewModel.play(music) } },
            onDismiss = { actionTarget = null },
        )
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
                            // 与「移动到」「定时停止」等对话框选项统一交互语言：
                            // 去掉系统方角涟漪、补 0.97 按压缩放、Role.Button 语义与减弱动效静帧降级；
                            // 这是全站最后一处裸露的对话框选项 clickable（其余均已接入 dialogOptionClickable）。
                            // 行内 RadioButton 保留原选中态与点击，整行点击仅扩大命中区，不改变选择逻辑。
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .dialogOptionClickable { picked = list.id }
                                    .padding(vertical = 6.dp),
                            ) {
                                RadioButton(selected = picked == list.id, onClick = { picked = list.id })
                                // 超长列表名单行省略：避免撑破对话框选项行、把确定/取消按钮挤出屏幕
                                Text(
                                    list.name,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
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

    // 错误提示改为可关闭对话框：此前是常驻红字，加入失败后会一直挂在页首且无法关闭。
    viewModel.errorMessage?.let { err ->
        GlassDialog(onDismissRequest = { viewModel.clearError() }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(err, color = MiuixTheme.colorScheme.error, fontSize = MiuixTheme.textStyles.body1.fontSize)
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { viewModel.clearError() }) { Text("好") }
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
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.ItemGap),
        onClick = onPlay,
        // 长按改开统一歌曲操作菜单：菜单首项即「添加到我的列表」，
        // 原长按直接进选列表面板的行为不丢，同时补上稍后播放/复制歌名/歌曲详情/不喜欢等入口
        onLongClick = onAddToList,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            // 整卡已可点且读屏会播报歌名/歌手，这里只是装饰图标；描述置空避免重复播报「播放」
            Icon(MiuixIcons.Play, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(10.dp))
            TrackCover(url = music.meta.picUrl, size = Dimens.CoverRow, corner = Dimens.CornerS)
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    music.name,
                    fontSize = MiuixTheme.textStyles.main.fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clickableNoIndication(onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    MiuixIcons.Delete,
                    contentDescription = "移除",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier {
    // remember 住 interactionSource，避免每次重组都新建对象导致按压状态丢失
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // 与全站图标按钮（BackIconButton / PlayerIconButton）一致的按压缩放：
    // 此前删除图标 indication=null 且无任何反馈，是全站唯一「点下去毫无变化」的可点元素。
    // 只动 transform（不触发重新布局）；减弱动效时退化为静帧。
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.85f else 1f,
        animationSpec = Motion.press(),
        label = "deletePress",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        // 与全站图标按钮一致补 Role.Button：
        // 内层 Icon 的 contentDescription（移除）读屏会报成「图像」，加 role 后才是「按钮」。
        .clickable(
            indication = null,
            interactionSource = interactionSource,
            role = Role.Button,
            onClick = onClick,
        )
}
