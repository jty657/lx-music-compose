package com.lingxu.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lingxu.music.LxApp
import com.lingxu.music.core.db.Playlist
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.AddFolder
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Playlist
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 歌曲更多操作菜单（对应原版列表项长按弹出的 Menu）。
 *
 * ### 为什么要有它
 * 原版任意列表（搜索 / 榜单 / 歌单 / 我的列表）长按歌曲会弹出「播放 / 稍后播放 / 添加到 /
 * 复制歌名 / 歌曲详情 / 不喜欢」。移植版此前每行只有一个「收藏」图标，
 * 用户无法在不播放的情况下「稍后播放」「复制歌名」「查看歌曲详情」，也无法把当前正在听的
 * 歌加入「不喜欢」——只能在设置页手打规则，体验与原版差距最明显。
 *
 * ### 为什么用 GlassDialog 而不是 Miuix DropdownMenu
 * 原版是弹在长按位置附近的浮层。Miuix 的 `WindowDropdownMenu` 用 value class（Dp）做参数，
 * 在 Kotlin 侧调用依赖精确的参数名，且本项目从未使用过；手写 Popup + 位置计算又要处理
 * 边界翻转、点外部关闭、返回键等一堆细节，属于「为样式引入新风险」。
 * 这里复用全站既有的 [GlassDialog]（同样的 scrim、圆角、进出场动画、深色适配与
 * 减弱动效降级），零新增依赖、零新 API 猜测，视觉与其他弹窗完全同语言。
 *
 * ### 与核心逻辑的边界
 * - 只调用既有仓库方法（player.playQueue / playNext / addToQueue、myLists.addMusics、
 *   dislike.add、share.share），不新增状态结构、不动路由；
 * - 「添加到我的列表」在弹窗内切二级面板，选完即关闭并回调提示，不影响页面层级。
 *
 * @param music 目标歌曲
 * @param onPlay 点「播放」时回调（各页面自己知道当前的上下文队列，交给页面播放才不会丢队列）
 * @param onDismiss 关闭菜单
 */
@Composable
fun SongActionMenu(
    music: MusicInfo,
    onPlay: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = LxApp.app
    // 二级面板：null = 主菜单；非空 = 「添加到我的列表」的歌单选择
    var pickingList by remember { mutableStateOf(false) }
    var lists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var tip by remember { mutableStateOf<String?>(null) }
    val isLocal = music.source == SourceConst.LOCAL
    val disliked = remember(music.id) { runCatching { app.dislikeRepository.isDisliked(music) }.getOrDefault(false) }
    // 是否已缓存播放链接：决定「清除链接缓存」是否可用。
    // 订阅队列并在每次变化时重算（链接解析完成后菜单会立刻变为可用），
    // 但只保留布尔值，不会因播放进度变化反复重组。
    val hasCachedUrl by remember(music.id) {
        app.playerRepository.state.map { st -> st.queue.any { it.id == music.id && !it.meta.filePath.isNullOrBlank() } }
            .distinctUntilChanged()
    }.collectAsState(initial = false)
    // 缓存的封面在磁盘 OkHttp 缓存里，与「链接缓存」是两件事；
    // 原版的「清除缓存」也只清链接，这里保持一致。

    // 只在需要展示歌单选择时才读一次磁盘：避免每次打开菜单都做 IO
    LaunchedEffect(pickingList) {
        if (pickingList) lists = runCatching { app.myListsRepository.load() }.getOrDefault(emptyList())
    }

    /** 统一的「执行 → 关菜单 → 提示」收尾，避免每项各写一遍 */
    fun finishAsync(tipText: String, block: suspend () -> Unit) {
        scope.launch {
            val result = runCatching { block() }
            tip = result.exceptionOrNull()?.let { "操作失败：${it.message}" } ?: tipText
        }
    }

    GlassDialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 4.dp)
        ) {
            Text("歌曲操作", fontSize = MiuixTheme.textStyles.title1.fontSize)
            Spacer(Modifier.size(4.dp))
            Text(
                "${music.name} - ${music.singer}",
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 音质与来源一行：原版列表项的音质角标在长按菜单里补上，
            // 让用户在做「复制 / 分享 / 看详情」决策前先知道这条结果是什么档次
            val qualities = remember(music.id) {
                music.qualitys.mapNotNull { it.type }.distinct()
            }
            if (qualities.isNotEmpty() || isLocal) {
                Text(
                    if (isLocal) "本地文件" else "可用音质：" + qualities.joinToString(" / "),
                    fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.size(8.dp))

            if (!pickingList) {
                MenuRow(MiuixIcons.Play, "播放") {
                    onPlay()
                    onDismiss()
                }
                MenuRow(MiuixIcons.Playlist, "下一首播放") {
                    app.playerRepository.playNext(music)
                    tip = "已添加到下一首播放"
                }
                MenuRow(MiuixIcons.Playlist, "加入播放队列") {
                    app.playerRepository.addToQueue(music)
                    tip = "已加入播放队列"
                }
                MenuRow(MiuixIcons.Copy, "复制歌名") {
                    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as? android.content.ClipboardManager
                    cm?.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "歌曲信息",
                            "${music.name} - ${music.singer}",
                        ),
                    )
                    tip = "已复制歌名"
                }
                MenuRow(MiuixIcons.Link, "歌曲详情") {
                    finishAsync("已打开歌曲详情") {
                        // 优先用内置 SDK 的平台详情页地址（与原版 getMusicDetailPageUrl 一致）；
                        // 拿不到时退回本地的链接拼装，保证离线/用户音源场景也有入口
                        val url = app.musicRepository.getMusicDetailPageUrl(music)
                            ?: app.shareRepository.songPageUrl(music).takeIf { it.isNotBlank() }
                            ?: throw IllegalStateException("该音源没有歌曲详情页")
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(url),
                            ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }
                MenuRow(MiuixIcons.AddFolder, "添加到我的列表") {
                    pickingList = true
                }
                // 本地歌曲的「分享」没有平台链接，仍可用（只分享歌名-歌手），与原版一致
                MenuRow(MiuixIcons.Share, "分享") {
                    finishAsync("分享") {
                        val type = app.settings.shareType.first()
                        app.shareRepository.share(music, type)
                    }
                    onDismiss()
                }
                if (!isLocal) {
                    MenuRow(
                        MiuixIcons.Reset,
                        "清除链接缓存",
                        summary = if (hasCachedUrl) null else "无缓存",
                        enabled = hasCachedUrl,
                    ) {
                        finishAsync("已清除播放链接缓存，下次播放将重新取链") {
                            if (!app.playerRepository.clearMusicUrl(music.id)) {
                                throw IllegalStateException("没有可清除的链接缓存")
                            }
                        }
                    }
                }
                if (!isLocal) {
                    MenuRow(
                        MiuixIcons.Blocklist,
                        if (disliked) "已在不喜欢列表" else "不喜欢这首歌",
                        enabled = !disliked,
                    ) {
                        val rule = if (music.singer.isBlank()) music.name else "${music.name}@${music.singer}"
                        finishAsync("已加入不喜欢列表，之后会自动跳过") {
                            app.dislikeRepository.add(rule)
                            // 正在播的就是这首时立即跳到下一首（对齐原版 handleDislikeMusic → playNext）
                            if (app.playerRepository.state.value.current?.id == music.id) {
                                app.playerRepository.next()
                            }
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("关闭") }
                }
            } else {
                // ---- 二级面板：选择要添加到的本地歌单 ----
                if (lists.isEmpty()) {
                    Text(
                        "还没有本地歌单，请先在「我的列表」新建",
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                lists.forEach { list ->
                    MenuRow(MiuixIcons.Playlist, list.name, summary = "${list.musics.size} 首") {
                        finishAsync("已添加到「${list.name}」") {
                            val location = app.settings.addMusicLocation.first()
                            app.myListsRepository.addMusics(list.id, listOf(music), location)
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { pickingList = false }) { Text("返回") }
                }
            }
        }
    }

    // 操作完成的轻提示：复用统一弹窗，不引入 Snackbar/Toast 两套提示体系
    tip?.let { msg ->
        GlassDialog(onDismissRequest = { tip = null; onDismiss() }) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(msg, fontSize = MiuixTheme.textStyles.body1.fontSize)
                Spacer(Modifier.size(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { tip = null; onDismiss() }) { Text("好") }
                }
            }
        }
    }
}

/**
 * 菜单行：左图标 + 文案（+ 可选副标题）。
 *
 * 用 [dialogOptionClickable] 与其他弹窗选项共享手感（按压缩放、无方角涟漪、Role.Button、
 * 减弱动效静帧），热区整行 48dp 以上，符合无障碍最小点击区。
 */
@Composable
private fun MenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) MiuixTheme.colorScheme.onSurface
    else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.dialogOptionClickable(onClick) else Modifier)
            .padding(vertical = 12.dp, horizontal = 4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        Text(
            text,
            fontSize = MiuixTheme.textStyles.body1.fontSize,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (summary != null) {
            Text(
                summary,
                fontSize = MiuixTheme.textStyles.footnote1.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
