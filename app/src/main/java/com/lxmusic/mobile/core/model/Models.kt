package com.lxmusic.mobile.core.model

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
    val ONLINE = listOf(KW, KG, TX, WY, MG, XM)
    fun isUserApi(id: String) = id.startsWith("user_api")
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
)

/** 热搜词 */
@Serializable
data class HotSearchWord(
    val source: String,
    val text: String,
    val num: Int = 0,
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
