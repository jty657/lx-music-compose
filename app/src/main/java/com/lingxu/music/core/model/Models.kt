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

    // ===== 音源显示名类型（对齐原版 common.sourceNameType）=====
    // 用 Compose State 承载全局类型：所有 displayName() 调用点都在 Composable 中，
    // 读取该 State 后会自动订阅，切换「原名/别名」时全站音源名即时重组刷新，
    // 而 10 处调用点无需任何改动。非 Composable 上下文（如 ViewModel）读取也安全。
    private val _nameType = androidx.compose.runtime.mutableStateOf(NAME_REAL)
    val nameType: String get() = _nameType.value

    /** 由设置层在启动/切换时同步；非法值一律回退到「原名」 */
    fun setNameType(value: String) {
        _nameType.value = if (value == NAME_ALIAS) NAME_ALIAS else NAME_REAL
    }

    const val NAME_REAL = "real"
    const val NAME_ALIAS = "alias"

    /** 原名（对应原版 sourceNameType=real，也是本项目历史默认显示） */
    private val realNames = mapOf(
        KW to "酷我音乐", KG to "酷狗音乐", TX to "QQ音乐",
        WY to "网易音乐", MG to "咪咕音乐", XM to "虾米音乐",
        LOCAL to "本地音乐", "all" to "聚合搜索",
    )

    /** 别名（对应原版 sourceNameType=alias，与原版 zh-cn.json 的 source_alias_* 一致） */
    private val aliasNames = mapOf(
        KW to "小蜗音乐", KG to "小枸音乐", TX to "小秋音乐",
        WY to "小芸音乐", MG to "小蜜音乐", XM to "虾米音乐",
        LOCAL to "本地音乐", "all" to "聚合搜索",
    )

    /**
     * 内置源显示名：按当前「原名/别名」设置取值。
     * 未收录的源（如 user_api_xxx）原样返回 id，交由调用方展示自定义名称。
     */
    fun displayName(source: String): String =
        (if (_nameType.value == NAME_ALIAS) aliasNames else realNames)[source] ?: source
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
