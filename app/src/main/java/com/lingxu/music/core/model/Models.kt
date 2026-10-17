package com.lingxu.music.core.model

import kotlinx.serialization.Serializable

/** 音质类型，与原版 LX.Quality 一致 */
typealias Quality = String

object QualityConst {
    const val Q128 = "128k"
    const val Q192 = "192k"
    const val Q320 = "320k"
    const val APE = "ape"
    const val FLAC = "flac"
    const val WAV = "wav"
    const val FLAC24 = "flac24bit"
    val ALL = listOf(Q128, Q192, Q320, APE, FLAC, WAV, FLAC24)
}

/** 音源：kw/kg/tx/wy/mg/xm/local + user_api_xxx */
typealias Source = String

object SourceConst {
    const val KW = "kw"
    const val KG = "kg"
    const val TX = "tx"
    const val WY = "wy"
    const val MG = "mg"
    const val XM = "xm"
    const val LOCAL = "local"
    val ONLINE = listOf(KW, KG, TX, WY, MG)
    fun isUserApi(id: String) = id.startsWith("user_api")

    /** 内置源中文名（对应原版 sourceNameType=alias） */
    fun displayName(source: String): String = when (source) {
        KW -> "酷我音乐"
        KG -> "酷狗音乐"
        TX -> "QQ音乐"
        WY -> "网易音乐"
        MG -> "咪咕音乐"
        XM -> "虾米音乐"
        LOCAL -> "本地音乐"
        "all" -> "聚合搜索"
        else -> source
    }
}

/** 歌曲信息，与原版 LX.Music.MusicInfo 对齐 */
@Serializable
data class MusicInfo(
    val id: String,
    val name: String,
    val singer: String,
    val source: Source,
    val interval: String? = null,
    val meta: MusicMeta = MusicMeta(),
    /** 音质大小映射，key 为音质 */
    val _qualitys: Map<String, QualitySize> = emptyMap(),
    /** 有序音质列表 */
    val qualitys: List<MusicQuality> = emptyList(),
    /** kg 源 hash */
    val hash: String? = null,
    /** tx 源 strMediaMid */
    val strMediaMid: String? = null,
    /** mg 源 */
    val copyrightId: String? = null,
    val lrcUrl: String? = null,
    val mrcUrl: String? = null,
    val trcUrl: String? = null,
)

@Serializable
data class MusicQuality(
    val type: Quality,
    val size: String? = null,
    val hash: String? = null,
)

@Serializable
data class QualitySize(
    val size: String? = null,
    val hash: String? = null,
)

@Serializable
data class MusicMeta(
    val songId: String = "",
    val albumName: String = "",
    val picUrl: String? = null,
    val albumId: String? = null,
    val filePath: String? = null,
    val ext: String? = null,
)

/** 歌词信息，与原版 LyricInfo 对齐 */
@Serializable
data class LyricInfo(
    val lyric: String,
    val tlyric: String? = null,
    val rlyric: String? = null,
    val lxlyric: String? = null,
)

/** 搜索页信息 */
@Serializable
data class SearchPage(
    val allPage: Int = 0,
    val limit: Int = 30,
    val page: Int = 1,
    val source: Source = "",
    val text: String = "",
    val list: List<MusicInfo> = emptyList(),
    val total: Int = 0,
)

/** 榜单 */
@Serializable
data class Board(
    val id: String,
    val name: String,
    val bangid: String = "",
)

/** 榜单详情 */
@Serializable
data class BoardDetail(
    val id: String,
    val name: String,
    val list: List<MusicInfo> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val maxPage: Int = 1,
)

/** 热搜词 */
@Serializable
data class HotSearchWord(
    val source: String,
    val text: String,
    val num: Int = 0,
)

/** 歌单项（对应原版 ListInfoItem） */
@Serializable
data class SongListInfo(
    val id: String,
    val name: String,
    val author: String = "",
    val img: String? = null,
    val playCount: String? = null,
    val total: String? = null,
    val desc: String? = null,
    val source: String,
)

/** 歌单列表分页结果（对应原版 ListInfo） */
@Serializable
data class SongListPage(
    val list: List<SongListInfo> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 30,
    val maxPage: Int = 1,
    val source: String = "",
    val tagId: String = "",
    val sortId: String = "",
)

/** 歌单标签组（对应原版 TagInfoTypeItem） */
@Serializable
data class SongListTagGroup(
    val name: String,
    val list: List<SongListTag> = emptyList(),
)

/** 歌单标签（对应原版 TagInfoItem） */
@Serializable
data class SongListTag(
    val id: String,
    val name: String,
    val parentId: String = "",
    val parentName: String = "",
    val source: String,
)

/** 歌单标签信息（对应原版 TagInfo） */
@Serializable
data class SongListTagInfo(
    val tags: List<SongListTagGroup> = emptyList(),
    val hotTag: List<SongListTag> = emptyList(),
    val source: String,
)

/** 歌单详情（对应原版 ListDetailInfo） */
@Serializable
data class SongListDetail(
    val list: List<MusicInfo> = emptyList(),
    val source: String,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 30,
    val maxPage: Int = 1,
    val id: String = "",
    val name: String = "",
    val img: String? = null,
    val desc: String? = null,
    val author: String = "",
    val playCount: String? = null,
)

/** 音源脚本信息（对应原版 UserApiInfo） */
@Serializable
data class ApiSourceInfo(
    val id: String,
    val name: String,
    val description: String = "",
    val author: String = "",
    val homepage: String = "",
    val version: String = "",
    val allowShowUpdateAlert: Boolean = true,
    /** 从脚本注释解析出的信息 */
    val meta: ScriptMeta = ScriptMeta(),
)

@Serializable
data class ScriptMeta(
    val name: String = "",
    val description: String = "",
    val author: String = "",
    val homepage: String = "",
    val version: String = "",
)
