package com.lingxu.music.core.repository

import android.content.Context
import android.content.Intent
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst

/**
 * 分享仓库（对应原版 common.shareType）。
 * - link：歌曲信息 + 平台网页链接
 * - text：纯文本「歌名 - 歌手」
 */
class ShareRepository(private val context: Context) {

    fun share(musicInfo: MusicInfo, type: String) {
        val text = when (type) {
            "link" -> "${musicInfo.name} - ${musicInfo.singer}\n${songPageUrl(musicInfo)}"
            else -> "${musicInfo.name} - ${musicInfo.singer}"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, musicInfo.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, "分享歌曲").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /** 各平台歌曲详情页（用于 link 分享） */
    private fun songPageUrl(musicInfo: MusicInfo): String {
        val id = musicInfo.meta.songId
        return when (musicInfo.source) {
            SourceConst.KG -> "https://www.kugou.com/song/#hash=$id"
            SourceConst.KW -> "https://www.kuwo.cn/play_detail/$id"
            SourceConst.WY -> "https://music.163.com/#/song?id=$id"
            SourceConst.TX -> "https://y.qq.com/n/ryqq/songDetail/$id"
            SourceConst.MG -> "https://music.migu.cn/v3/music/song/$id"
            else -> ""
        }
    }
}
