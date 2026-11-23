package com.lingxu.music.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.ui.components.ContentMaxWidth
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.FilterChip
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.GlassTopBar
import com.lingxu.music.ui.components.LoadMoreButton
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.components.TextActionButton
import com.lingxu.music.ui.components.TrackCover
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.MotionBaseMs
import com.lingxu.music.ui.theme.animateListItem
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun SearchScreen(initialKeyword: String = "", viewModel: SearchViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val textFieldState = remember { TextFieldState() }
    val sources = listOf("all") + SourceConst.ONLINE
    var selectedSource by rememberSaveable { mutableStateOf("all") }

    // 由热词/历史进入时自动搜索
    androidx.compose.runtime.LaunchedEffect(initialKeyword) {
        if (initialKeyword.isNotBlank()) {
            textFieldState.setTextAndPlaceCursorAtEnd(initialKeyword)
            viewModel.search(initialKeyword, selectedSource)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // 搜索输入框（磨砂玻璃条）
        GlassTopBar {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.PageH, vertical = 8.dp),
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
        }

        Column(
            Modifier
                .fillMaxSize()
                // 大屏宽度上限：内容不再被拉伸到 1000dp+，手机端完全无变化
                .widthIn(max = ContentMaxWidth)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
        Spacer(Modifier.height(8.dp))

        // 音源选择（横向滚动：6 个按钮挤在固定 Row 里会溢出屏幕）
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(sources) { source ->
                // 统一走 FilterChip：胶囊形 + 选中主色描边 + 168ms 底色渐变 + 读屏 selected 语义
                FilterChip(
                    label = SourceConst.displayName(source),
                    selected = selectedSource == source,
                    onClick = { selectedSource = source },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 搜索类型切换（对应原版 音乐/歌单）。
        // 原先两个 Button 容器相同，只靠文字颜色区分选中态，扫视时看不出当前在哪一栏；
        // 改为与上方音源 chip 同一套「选中 = 主色淡底 + 主色文字」的玻璃胶囊。
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(SearchType.MUSIC to "音乐", SearchType.SONGLIST to "歌单").forEach { (type, label) ->
                FilterChip(
                    label = label,
                    selected = viewModel.searchType == type,
                    onClick = { viewModel.setType(type) },
                    horizontalPadding = 18,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 错误提示统一放在结果列表底部的可点「重试项」展示（见下方两个分支），
        // 这里不再单独渲染红字，避免加载失败时同一句错误文案出现两遍。

        // 搜索历史（无结果且输入为空时展示；可在设置里关闭）
        // initial 与落盘默认（SettingsRepository: false）一致，避免首帧先闪出历史再消失。
        val showHistory by LxApp.app.settings.isShowHistorySearch.collectAsState(initial = false)
        // 热搜同样受设置开关门控（对齐原版 BlankView 的 isShowHotSearch）：
        // 此前搜索页遗漏该开关，用户关掉「显示热搜」后热搜依旧出现。
        val showHot by LxApp.app.settings.isShowHotSearch.collectAsState(initial = false)
        // 必须加「输入框为空」判断：搜索后输入框仍保留关键词，若只判结果为空，
        // 任意无结果的搜索都会命中历史分支并 return，导致「没有找到相关音乐」空态永远不显示。
        val inputIsBlank = textFieldState.text.toString().isBlank()
        if (showHistory && inputIsBlank && viewModel.results.isEmpty() && viewModel.songLists.isEmpty() && viewModel.history.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("搜索历史", fontSize = MiuixTheme.textStyles.body2.fontSize)
                Spacer(Modifier.weight(1f))
                Text(
                    "长按删除单条",
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.size(8.dp))
                // 统一走 TextActionButton：48dp 命中区 + 按压缩放 + Role.Button + 静帧降级，
                // 取代此前裸 clickable 的方角涟漪（字号/颜色/视觉位置保持原样）
                TextActionButton(
                    text = "清空",
                    onClick = { viewModel.clearHistory() },
                    color = MiuixTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            // 历史最多 30 条，小屏上平铺会超出屏幕且下方条目不可见；用 weight + 滚动承载
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                viewModel.history.forEach { word ->
                    Text(
                        word,
                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                        // 历史搜索词单行省略，避免超长词换行改变行高、挤掉下方条目。
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            // 与全站 unified 交互语言一致：indication=null 去掉方角涟漪 +
                            // 轻微按压缩放 + Role.Button + 读屏描述（含长按删除提示）+ 静帧降级。
                            // 此前是全站唯一一处裸 combinedClickable 的历史词条，与
                            // BackIconButton / TextActionButton / 各列表操作键手感不一致。
                            .historyRowClickable(
                                description = "搜索历史：$word，长按删除",
                                onClick = {
                                    textFieldState.setTextAndPlaceCursorAtEnd(word)
                                    scope.launch { viewModel.search(word, selectedSource) }
                                },
                                onLongClick = { viewModel.removeHistory(word) },
                            )
                            .padding(vertical = 6.dp),
                    )
                }
            }
            return@Column
        }

        // 热搜快捷入口（对齐原版搜索页热搜）：当没有可展示的历史、且输入为空/未搜索时，
        // 展示内置 SDK 热搜词，点击即填入并搜索（与历史词同一路径），省去用户手动输入。
        // showHot 门控对齐原版 isShowHotSearch：关掉「显示热搜」后不再展示。
        if (showHot && inputIsBlank && !viewModel.hasSearched && viewModel.results.isEmpty()
            && viewModel.songLists.isEmpty() && viewModel.hotWords.isNotEmpty()
        ) {
            // 进场淡入：只动 alpha（不触发布局），减弱动效时 0ms 到位，与全站列表项淡入同一语言。
            val hotReduceMotion = LocalReduceMotion.current
            val hotAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = if (hotReduceMotion) 0 else MotionBaseMs),
                label = "hotEntryFade",
            )
            Column(Modifier.graphicsLayer { alpha = hotAlpha }) {
                Text("热搜", fontSize = MiuixTheme.textStyles.title1.fontSize)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.hotWords) { word ->
                        FilterChip(
                            label = word,
                            selected = false,
                            onClick = {
                                textFieldState.setTextAndPlaceCursorAtEnd(word)
                                scope.launch { viewModel.search(word, selectedSource) }
                            },
                        )
                    }
                }
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        // 结果列表
        val results = viewModel.results
        val songLists = viewModel.songLists
        if (songLists.isNotEmpty()) {
            val songListState = rememberLazyListState()
            // 与音乐结果分支一致：歌单结果滚动也要上报，迷你播放条才会随滚动隐藏
            com.lingxu.music.ui.components.ReportScrollForMiniBar(songListState)
            // 滚到接近底部时自动加载下一页（留 4 项余量，用户不必伸手点按钮）
            AutoLoadMore(songListState, viewModel.hasMore, viewModel.loading) { viewModel.loadMore() }
            LazyColumn(state = songListState, modifier = Modifier.weight(1f)) {
                items(songLists, key = { "${it.source}_${it.id}" }) { item ->
                    GlassSurface(
                        modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                        onClick = {
                            navigator.push(Screen.SongListDetail(item.source, item.id, item.name))
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            // 歌单结果此前用「收藏」图标做占位，语义上完全不相关（点了也不会收藏）。
                            // 换成 Layers（歌单/合集），与首页「我的列表」「歌单广场」同一图标语言，
                            // 并用柔和的次要色圆底托住，避免和右侧可点的收藏按钮混淆。
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(Dimens.CornerM))
                                    .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    MiuixIcons.Layers,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(
                                    item.name,
                                    fontSize = MiuixTheme.textStyles.main.fontSize,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "${item.author} · ${SourceConst.displayName(item.source)}",
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                if (viewModel.hasMore) {
                    item(key = "__load_more__") {
                        LoadMoreButton(
                            loading = viewModel.loading,
                            onClick = { viewModel.loadMore() },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                    }
                }
                // 自动加载失败时给一个可点的重试入口，否则用户会一直卡在列表底部
                viewModel.errorMessage?.let { err ->
                    item(key = "__load_error__") {
                        RetryHint(text = err, onClick = { viewModel.loadMore() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            return@Column
        } else if (viewModel.searchType == SearchType.SONGLIST &&
            !viewModel.loading && viewModel.hasSearched && viewModel.errorMessage == null
        ) {
            // 歌单搜索无结果：此前会 fall through 到音乐分支误显「没有找到相关音乐」，
            // 这里给出对应歌单的空态后直接返回，避免误导。
            EmptyHint(icon = MiuixIcons.Favorites, text = "没有找到相关歌单")
            return@Column
        }

        val resultState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(resultState)
        AutoLoadMore(resultState, viewModel.hasMore, viewModel.loading) { viewModel.loadMore() }
        // 搜索后无结果：给出明确空态，避免只剩一片空白让用户以为卡住
        if (results.isEmpty() && !viewModel.loading && viewModel.hasSearched && viewModel.errorMessage == null) {
            EmptyHint(icon = MiuixIcons.Search, text = "没有找到相关音乐")
        }
        LazyColumn(state = resultState, modifier = Modifier.weight(1f)) {
            items(results, key = { it.id }) { music ->
                SearchResultCard(
                    modifier = animateListItem(),
                    music = music,
                    isFavorite = music.id in viewModel.favoriteIds,
                    onClick = {
                        scope.launch {
                            viewModel.play(music)
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(music) },
                )
            }
            if (viewModel.hasMore) {
                item(key = "__load_more__") {
                    LoadMoreButton(
                        loading = viewModel.loading,
                        onClick = { viewModel.loadMore() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                }
            }
            viewModel.errorMessage?.let { err ->
                item(key = "__load_error__") {
                    RetryHint(text = err, onClick = { viewModel.loadMore() }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        }
    }
}

@Composable
private fun SearchResultCard(
    music: MusicInfo,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            // 整卡已可点且读屏会播报歌名/歌手，这里只是装饰图标；描述置空避免重复播报「播放」
            Icon(MiuixIcons.Play, contentDescription = null, modifier = Modifier.size(32.dp))
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
                Text(
                    "${music.singer} · ${music.interval ?: ""}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // 可用音质标签（对应原版列表项的音质角标）
                val qualities = music.qualitys.mapNotNull { it.type }.distinct()
                if (qualities.isNotEmpty()) {
                    Text(
                        qualities.joinToString(" "),
                        fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
            // 与播放页收藏键一致：48dp 热区 + 按压缩放反馈 + 收藏/取消图标淡变，
            // 消除此前「点下去毫无变化、图标硬切」的不一致。减弱动效时退化为静帧。
            FavoriteToggleIcon(isFavorite = isFavorite, onClick = onToggleFavorite)
        }
    }
}

/**
 * 搜索结果的收藏/取消收藏图标：48dp 热区 + 按压缩放 + 图标淡变。
 * 与播放页 PlayerIconButton 的收藏键同一手感，此前本按钮是最后一处「无反馈 + 图标硬切」的可点收藏图标。
 * 减弱动效时缩放退化为静帧、图标瞬时切换。
 */
@Composable
private fun FavoriteToggleIcon(isFavorite: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.85f else 1f,
        animationSpec = Motion.press(),
        label = "favoriteTogglePress",
    )
    Box(
        Modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                indication = null,
                interactionSource = interaction,
                // 与 BackIconButton / PlayerIconButton 一致补 Role.Button：
                // 内层 Icon 的 contentDescription（收藏/取消收藏）读屏会报成「图像」，加 role 后才是「按钮」。
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = isFavorite,
            animationSpec = tween(durationMillis = if (reduceMotion) 0 else 180),
            label = "favoriteToggleIcon",
        ) { fav ->
            Icon(
                if (fav) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                contentDescription = if (fav) "取消收藏" else "收藏",
                tint = if (fav) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * 搜索历史词条的点击修饰：与全站交互语言统一。
 *
 * 此前历史词条是全站最后一处裸 [combinedClickable]——系统方角涟漪铺满整行、按下无任何
 * 缩放反馈、读屏只当普通文本且不知可长按删除。这里与 [BackIconButton]、[TextActionButton]
 * 及各页操作键对齐：`indication=null` 去掉方角涟漪、轻微按压缩放（只动 transform）、
 * `Role.Button` + 读屏描述、减弱动效时退化为静帧。onClick/onLongClick 行为完全不变。
 */
@Composable
private fun Modifier.historyRowClickable(
    description: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.98f else 1f,
        animationSpec = Motion.press(),
        label = "historyRowPress",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        .semantics { contentDescription = description }
}

/** 滚到接近底部（留 4 项余量）时自动触发加载下一页 */
@Composable
private fun AutoLoadMore(
    listState: androidx.compose.foundation.lazy.LazyListState,
    hasMore: Boolean,
    loading: Boolean,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState, hasMore, loading) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            val last = visible.lastOrNull()?.index ?: 0
            last to listState.layoutInfo.totalItemsCount
        }.collect { (last, total) ->
            if (hasMore && !loading && total > 0 && last >= total - 4) {
                onLoadMore()
            }
        }
    }
}
