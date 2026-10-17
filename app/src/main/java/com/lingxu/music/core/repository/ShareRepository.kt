package com.lingxu.music.core.repository

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst

/**
 * 分享仓库（对应原版 common.shareType）。
 * - system：系统分享（内容含歌曲信息 + 平台链接）
 * - clipboard：复制到剪贴板
 */
class ShareRepository(private val context: Context) {

    fun share(musicInfo: MusicInfo, type: String) {
        val text = "${musicInfo.name} - ${musicInfo.singer}\n${songPageUrl(musicInfo)}".trimEnd()
        if (type == "clipboard") {
            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            if (manager == null) {
                Toast.makeText(context, "复制失败：无法访问剪贴板", Toast.LENGTH_SHORT).show()
                return
            }
            manager.setPrimaryClip(ClipData.newPlainText(musicInfo.name, text))
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, musicInfo.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        // 设备上没有任何能处理 text/plain 的应用时 createChooser 会抛 ActivityNotFoundException
        val result = runCatching {
            context.startActivity(
                Intent.createChooser(intent, "分享歌曲").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        if (result.isFailure) {
            Toast.makeText(context, "分享失败：没有可用的分享目标", Toast.LENGTH_SHORT).show()
        }
    }

    /** 各平台歌曲详情页（用于 link 分享） */
    private fun songPageUrl(musicInfo: MusicInfo): String {
        // 注意：kg 的 meta.songId 是 audio_id，不是 hash；mg 需要 copyrightId。
        // 用错字段拼出来的链接是死页（原版 kg/mg 分别用 songInfo.hash / copyrightId）。
        return when (musicInfo.source) {
            SourceConst.KG -> {
                val hash = musicInfo.hash?.takeIf { it.isNotBlank() } ?: return ""
                val albumId = musicInfo.meta.albumId.orEmpty()
                "https://www.kugou.com/song/#hash=$hash" +
                    if (albumId.isNotBlank()) "&album_id=$albumId" else ""
            }
            SourceConst.KW -> "https://www.kuwo.cn/play_detail/${musicInfo.meta.songId}"
            SourceConst.WY -> "https://music.163.com/#/song?id=${musicInfo.meta.songId}"
            SourceConst.TX -> "https://y.qq.com/n/ryqq/songDetail/${musicInfo.meta.songId}"
            SourceConst.MG -> {
                val copyrightId = musicInfo.copyrightId?.takeIf { it.isNotBlank() } ?: return ""
                "https://music.migu.cn/v3/music/song/$copyrightId"
            }
            else -> ""
        }
    }
}
