package com.lingxu.music.core.repository

import android.util.Log
import com.lingxu.music.core.engine.BuiltinSdkBridge
import com.lingxu.music.core.engine.ScriptEngineManager
import com.lingxu.music.core.engine.toOldMusicInfo
import com.lingxu.music.core.model.LyricInfo
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SourceConst
import com.lingxu.music.core.request.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * 音乐统一仓库：对应原版 core/music 系列。
 * - 搜索/榜单/热搜/歌词/封面：内置 SDK（kw/kg/tx/wy/mg）
 * - 播放 URL：用户音源脚本（musicUrl），内置源在无 api-list 时不可用（与原版一致）
 * - 换源 fallback：取 URL 失败时在其它源搜索同名歌曲并尝试
 */
class MusicRepository(
    private val sdk: BuiltinSdkBridge,
    private val engineManager: ScriptEngineManager,
    private val settings: SettingsRepository,
) {
    companion object {
        private const val TAG = "MusicRepository"
        val JSON = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        /** 歌词有效性校验（原版 existTimeExp） */
        val LRC_TIME_RXP = Regex("\\[\\d{1,2}:.*\\d{1,4}\\]")
    }

    private val _qualityList = MutableStateFlow<Map<String, List<String>>>(
        engineManager.builtinQualityList,
    )
    val qualityList: StateFlow<Map<String, List<String>>> = _qualityList

    private val sourceMutex = Mutex()

    /** 源是否可用（原版 assertApiSupport） */
    fun assertApiSupport(source: String): Boolean {
        if (source == SourceConst.LOCAL) return true
        return _qualityList.value.containsKey(source)
    }

    /** 当前全局音源（原版 settingState.setting['common.apiSource']） */
    private var _currentApiSource: String = "kw"

    /** 当前激活的自定义音源 id（没有则为空字符串，与内置音源区分） */
    private fun currentUserApiId(): String {
        val userApi = engineManager.currentApiId()
        return userApi ?: if (SourceConst.isUserApi(_currentApiSource)) _currentApiSource else ""
    }

    /** 设置当前音源（内置或 user_api） */
    fun setApiSource(apiSource: String) {
        _currentApiSource = apiSource
        if (SourceConst.isUserApi(apiSource)) {
            // 用户音源：qualityList 由脚本 inited 事件驱动
            _qualityList.value = emptyMap()
        } else {
            _qualityList.value = engineManager.builtinQualityList
        }
        sdk.setApiSource(apiSource)
    }

    /** 用户音源初始化完成时更新 qualityList（对应 handleInit 过滤逻辑） */
    fun updateUserApiCapabilities(caps: Map<String, JsonObject>) {
        val qualitys = mutableMapOf<String, List<String>>()
        val validQualitys = setOf("128k", "320k", "flac", "flac24bit")
        caps.forEach { (source, cap) ->
            val type = cap["type"]?.jsonPrimitive?.content ?: return@forEach
            if (type != "music") return@forEach
            val qs = (cap["qualitys"] as? JsonArray)
                ?.mapNotNull { it.jsonPrimitive.content }
                ?.filter { it in validQualitys }
                ?: emptyList()
            qualitys[source] = qs
        }
        _qualityList.value = qualitys
    }

    // ================= 搜索 =================

    suspend fun search(text: String, page: Int, source: String, limit: Int = 30): List<MusicInfo> =
        sdk.search(text, page, source, limit).list

    // ================= 榜单 =================

    suspend fun getBoardsList(source: String) = sdk.getBoardsList(source)
    suspend fun getBoardList(source: String, bangId: String, page: Int) =
        sdk.getBoardList(source, bangId, page)

    // ================= 热搜 =================

    suspend fun getHotSearch(source: String) = sdk.getHotSearch(source)

    // ================= 歌单 =================

    /** 歌单排序选项（原版 songlistState.sortList） */
    fun getSongListSorts(source: String) = sdk.getSongListSorts(source)

    /** 歌单标签 */
    suspend fun getSongListTags(source: String) = sdk.getSongListTags(source)

    /** 歌单列表 */
    suspend fun getSongList(source: String, sortId: String, tagId: String, page: Int) =
        sdk.getSongList(source, sortId, tagId, page)

    /** 歌单详情（歌单内歌曲） */
    suspend fun getSongListDetail(source: String, id: String, page: Int) =
        sdk.getSongListDetail(source, id, page)

    /** 搜索歌单（单源） */
    suspend fun searchSongList(text: String, page: Int, source: String, limit: Int = 18) =
        sdk.searchSongList(text, page, source, limit)

    /** 搜索歌单（聚合，对齐原版 core/search/songlist.ts） */
    suspend fun searchSongListAll(text: String, page: Int) = sdk.searchSongListAll(text, page)

    // ================= 歌词 =================

    suspend fun getLyricInfo(musicInfo: MusicInfo, allowToggleSource: Boolean = true): LyricInfo {
        if (SourceConst.isUserApi(currentUserApiId())) {
            // 用户音源处理 local 与各源歌词
            return try {
                val oldInfo = musicInfo.toOldMusicInfo()
                val result = engineManager.getLyric(musicInfo.source, oldInfo)
                LyricInfo(
                    lyric = result["lyric"]?.let { (it as? JsonPrimitive)?.content }
                        ?: throw IllegalStateException("invalid lyric"),
                    tlyric = result["tlyric"]?.let { (it as? JsonPrimitive)?.content },
                    rlyric = result["rlyric"]?.let { (it as? JsonPrimitive)?.content },
                    lxlyric = result["lxlyric"]?.let { (it as? JsonPrimitive)?.content },
                )
            } catch (e: Exception) {
                Log.e(TAG, "user api lyric failed: ${e.message}")
                // fallback 到内置 SDK
                sdk.getLyric(musicInfo.source, musicInfo.toOldMusicInfo())
            }
        }
        return sdk.getLyric(musicInfo.source, musicInfo.toOldMusicInfo())
    }

    // ================= 封面 =================

    suspend fun getPicUrl(musicInfo: MusicInfo, allowToggleSource: Boolean = true): String {
        musicInfo.meta.picUrl?.let { return it }
        if (SourceConst.isUserApi(currentUserApiId())) {
            // 用户音源可以为 local 提供封面
            try {
                return engineManager.getPic(musicInfo.source, musicInfo.toOldMusicInfo())
            } catch (e: Exception) {
                Log.e(TAG, "user api pic failed: ${e.message}")
            }
        }
        return try {
            sdk.getPic(musicInfo.source, musicInfo.toOldMusicInfo())
        } catch (e: Exception) {
            Log.e(TAG, "sdk pic failed: ${e.message}")
            throw e
        }
    }

    // ================= 播放 URL =================

    suspend fun getMusicUrl(
        musicInfo: MusicInfo,
        quality: String? = null,
        allowToggleSource: Boolean = true,
        onToggleSource: () -> Unit = {},
    ): String {
        if (musicInfo.source == SourceConst.LOCAL) {
            // 本地歌曲：用户音源的 local 分支，type 为 null（原版 apis('local').getMusicUrl(info, null)）
            val oldInfo = musicInfo.toOldMusicInfo()
            return try {
                engineManager.getMusicUrl(SourceConst.LOCAL, oldInfo, null)
            } catch (e: Exception) {
                Log.e(TAG, "local music url failed: ${e.message}")
                throw e
            }
        }

        val preferQuality = quality ?: settings.currentQuality()
        // 原版 handleGetOnlineMusicUrl: quality ?? getPlayQuality(playQuality, musicInfo)
        val targetQuality = getPlayQuality(musicInfo, preferQuality)

        return try {
            getOnlineMusicUrl(musicInfo, targetQuality)
        } catch (e: Exception) {
            Log.e(TAG, "get music url failed: ${e.message}")
            if (!allowToggleSource) throw e
            onToggleSource()
            // 换源 fallback：在其他源搜索同名歌曲
            findAndGetOtherSourceUrl(musicInfo, targetQuality)
                ?: throw e
        }
    }

    private suspend fun getOnlineMusicUrl(musicInfo: MusicInfo, targetQuality: String): String {
        if (SourceConst.isUserApi(currentUserApiId())) {
            val oldInfo = musicInfo.toOldMusicInfo()
            return engineManager.getMusicUrl(musicInfo.source, oldInfo, targetQuality)
        }
        // 内置源：走 SDK（api-list 为空时与原版一样报错）
        return sdk.getMusicUrl(musicInfo.source, musicInfo.toOldMusicInfo(), targetQuality)
    }

    /** 换源搜索并尝试取 URL（原版 findMusic + getOnlineOtherSourceMusicUrl 简化实现） */
    private suspend fun findAndGetOtherSourceUrl(musicInfo: MusicInfo, quality: String): String? {
        val searchText = "${musicInfo.name} ${musicInfo.singer}".trim()
        if (searchText.isEmpty()) return null
        val otherSources = listOf("kw", "kg", "tx", "wy", "mg").filter { it != musicInfo.source }
        val candidates = otherSources.flatMap { source ->
            runCatching { search(searchText, 1, source, 25) }.getOrElse { emptyList() }
        }.filter { candidate ->
            candidate.name == musicInfo.name || candidate.singer == musicInfo.singer
        }
        for (candidate in candidates.take(10)) {
            runCatching {
                return getOnlineMusicUrl(candidate, quality)
            }.onFailure { Log.w(TAG, "other source url failed: ${it.message}") }
        }
        return null
    }

    /** 原版 getPlayQuality：偏好为高音质档位时逐级下降，否则固定 128k */
    private fun getPlayQuality(musicInfo: MusicInfo, preferQuality: String): String {
        val list = _qualityList.value[musicInfo.source] ?: emptyList()
        return sdk.getPlayQuality(preferQuality, musicInfo, list)
    }

    /** 歌词有效性校验（原版 existTimeExp 逻辑） */
    fun isValidLyric(lyric: String): Boolean = LRC_TIME_RXP.containsMatchIn(lyric)

    /** 通用文本 GET（设置页等用） */
    suspend fun fetchText(url: String): String = HttpClient.instance.fetchText(url)
}
