package com.lingxu.music.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.Board
import com.lingxu.music.ui.components.ContentWidthLimit
import com.lingxu.music.ui.components.EmptyHint
import com.lingxu.music.ui.components.GlassSurface
import com.lingxu.music.ui.components.RetryHint
import com.lingxu.music.ui.navigation.LocalNavigator
import com.lingxu.music.ui.navigation.Screen
import com.lingxu.music.ui.theme.Dimens
import com.lingxu.music.ui.theme.LocalReduceMotion
import com.lingxu.music.ui.theme.Motion
import com.lingxu.music.ui.theme.animateListItem
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** 首页：音源切换入口 + 榜单 + 热搜 */
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val navigator = LocalNavigator.current
    val boards = viewModel.boards
    val hotSearch = viewModel.hotSearch
    val selectedSource = viewModel.selectedSource
    // 在设置/音源页切了音源后，首页的榜单/热搜也随之刷新
    val apiSource by LxApp.app.settings.apiSource.collectAsState(initial = "")
    LaunchedEffect(apiSource) { viewModel.refresh() }
    // 内容宽度上限：平板上不再把卡片拉成 1000dp 长条（手机端完全无变化）
    ContentWidthLimit(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        // 音源入口：主页唯一的「作用域级」入口，用主色淡染区分于下方并列的两张功能卡，
        // 否则三张卡材质完全一致，扫视时看不出主次（原版首页也是音源区最重）。
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            tint = MiuixTheme.colorScheme.primary.copy(alpha = 0.08f),
            onClick = { navigator.push(Screen.Source) },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                EntryIcon(MiuixIcons.File)
                Column(Modifier.padding(start = 12.dp)) {
                    Text("音源管理", fontSize = MiuixTheme.textStyles.main.fontSize)
                    Text(
                        viewModel.sourceLabel(),
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        // 自定义音源名可能很长，不加单行省略会换行把入口卡撑高，与下方两卡不齐。
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        // 我的列表 / 歌单广场入口
        Row(Modifier.fillMaxWidth()) {
            GlassSurface(
                modifier = Modifier.weight(1f),
                onClick = { navigator.push(Screen.MyLists) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    EntryIcon(MiuixIcons.Layers)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("我的列表", fontSize = MiuixTheme.textStyles.main.fontSize)
                        Text(
                            "本地歌单与收藏",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            // 与音源卡副标题一致：窄屏/大字号下单行省略，避免撑高卡片与右侧不齐
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
            GlassSurface(
                modifier = Modifier.weight(1f),
                onClick = { navigator.push(Screen.SongList(selectedSource)) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    EntryIcon(MiuixIcons.Music)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("歌单广场", fontSize = MiuixTheme.textStyles.main.fontSize)
                        Text(
                            "平台歌单",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            // 与音源卡副标题一致：窄屏/大字号下单行省略，避免撑高卡片与左侧不齐
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.size(12.dp))

        // 热搜（可在设置里关闭）。热搜与榜单并存：热搜占较小权重，榜单占剩余空间，
        // 避免此前「有热搜时榜单被完全屏蔽、用户看不到排行榜」的问题。
        val showHotSearch by LxApp.app.settings.isShowHotSearch.collectAsState(initial = true)
        val showHot = showHotSearch && hotSearch.isNotEmpty()
        if (showHot) {
            Text("热搜", fontSize = MiuixTheme.textStyles.title1.fontSize)
            Spacer(Modifier.size(4.dp))
            val hotState = rememberLazyListState()
            com.lingxu.music.ui.components.ReportScrollForMiniBar(hotState)
            LazyColumn(Modifier.weight(0.4f), state = hotState) {
                itemsIndexed(hotSearch) { index, word ->
                    // 热搜行是全站少数仍用裸 clickable 的列表项：系统方角涟漪会溢出整行宽度、
                    // 按下无反馈、读屏只当普通文本。这里与 BackIconButton / RetryHint / 各列表
                    // 操作键统一为「indication=null + 轻微按压缩放 + Role.Button + reduceMotion 静帧」。
                    val hotInteraction = remember { MutableInteractionSource() }
                    val hotPressed by hotInteraction.collectIsPressedAsState()
                    val hotReduceMotion = LocalReduceMotion.current
                    val hotScale by animateFloatAsState(
                        targetValue = if (hotPressed && !hotReduceMotion) 0.98f else 1f,
                        animationSpec = Motion.press(),
                        label = "hotSearchPress",
                    )
                    // 前三名用主色强调、字号放大一档，其余弱化为次要色，形成清晰的榜单层级
                    val isTop3 = index < 3
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        // 与同页榜单列表、全站其它 LazyColumn 统一：接入 animateListItem，
                        // 热搜词刷新时的增删/位移走全站一致的淡入淡出 + 柔和位移，
                        // 并在系统「减弱动效」时静帧到位。animateItem 放在 Modifier 链首位。
                        modifier = animateListItem()
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = hotScale; scaleY = hotScale }
                            .clip(RoundedCornerShape(Dimens.CornerS))
                            .clickable(
                                interactionSource = hotInteraction,
                                indication = null,
                                role = Role.Button,
                                onClick = { navigator.push(Screen.Search(word)) },
                            )
                            // 榜单每行热区提到 48dp 下限，视觉紧凑度基本不变
                            .padding(vertical = 14.dp)
                            .semantics { contentDescription = "热搜第 ${index + 1} 名：$word" },
                    ) {
                        Text(
                            "${index + 1}",
                            color = if (isTop3) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = if (isTop3) MiuixTheme.textStyles.body1.fontSize
                                       else MiuixTheme.textStyles.body2.fontSize,
                        )
                        // 热搜词来自网络、长度不可控：占满剩余宽度并单行省略，
                        // 避免长词换行、与左侧排名数字错位并撑高整行（与榜单项同一省略语言）。
                        Text(
                            word,
                            modifier = Modifier.weight(1f).padding(start = 12.dp),
                            color = if (isTop3) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurface,
                            fontSize = MiuixTheme.textStyles.body1.fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.size(12.dp))
        }
        // 榜单（始终显示）
        Text("排行榜", fontSize = MiuixTheme.textStyles.title1.fontSize)
        Spacer(Modifier.size(4.dp))
        // 失败/空态/加载中给出提示：此前 loading=true 时榜单区一片空白，用户以为没有榜单。
        // 与榜单详情、歌单详情的加载提示保持一致。
        if (boards.isEmpty()) {
            when {
                // 加载中：给 3 个与真实卡片同构的骨架（静态半透明，不做闪烁动画），
                // 既占住版面避免「空白 → 突然出现列表」的跳变，也不会在低端机上多一份逐帧绘制。
                // 高度与原卡片一致（行内 padding 8dp + 内容 ~28dp + 卡片描边），否则加载完成时列表会整体上跳。
                viewModel.loading -> Column(Modifier.fillMaxWidth()) {
                    repeat(3) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.ItemGap)
                                .height(60.dp)
                                .clip(RoundedCornerShape(Dimens.CardCorner))
                                .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.08f)),
                        )
                    }
                }
                viewModel.error != null -> RetryHint(text = "加载失败，点击重试", onClick = { viewModel.loadBoards() })
                else -> EmptyHint(icon = MiuixIcons.Music, text = "暂无榜单")
            }
        }
        val boardState = rememberLazyListState()
        com.lingxu.music.ui.components.ReportScrollForMiniBar(boardState)
        LazyColumn(Modifier.weight(if (showHot) 0.6f else 1f), state = boardState) {
                items(boards, key = { it.id }) { board ->
                    GlassSurface(
                        // animateItem 必须在 LazyItemScope 上调用，链式顺序需先于其它 Modifier
                        modifier = animateListItem().fillMaxWidth().padding(vertical = Dimens.ItemGap),
                        onClick = {
                            val (source, bangId, name) = viewModel.boardTarget(board)
                            navigator.push(Screen.BoardList(source, bangId, name))
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            IconWrap(MiuixIcons.Music)
                            // 长榜单名单行省略：与首页其余列表项一致，超长名称以省略号收尾，不撑破卡片。
                            // weight(1f, fill=false)：把剩余宽度交给名称收缩，overflow 才会真正生效，
                            // 否则 wrap 内容宽度时长名称会顶破卡片右侧。
                            Text(
                                board.name,
                                modifier = Modifier.padding(start = 12.dp).weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
    }
    }
}

/**
 * 首页入口卡的图标：与主内容同处的「淡色圆底托图标」语言对齐
 * （搜索页歌单结果此前已采用），24dp 视觉尺寸套一层 36dp 圆角淡底，
 * 让音源/我的列表/歌单广场三张入口卡的图标从裸图标升级为有层级的功能入口标识。
 * 仅用于入口卡；榜单项沿用裸 [IconWrap] 以保持列表行高度不变。纯视觉、不改交互。
 */
@Composable
private fun EntryIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(Dimens.CornerM))
            .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        top.yukonga.miuix.kmp.basic.Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun IconWrap(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    top.yukonga.miuix.kmp.basic.Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
}
