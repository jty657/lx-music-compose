package com.lingxu.music.core.db

import android.content.Context
import com.lingxu.music.core.model.ApiSourceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * JSON 文件存储（对应原版 AsyncStorage 方案）：
 * - api_source_list.json：音源元信息数组（原版 @user_api__ key）
 * - api_source_<id>.js：脚本原文（原版 @user_api__<id> key）
 * - playlist.json：歌单
 */
@Serializable
data class ApiSourceMeta(
    val id: String,
    val name: String,
    val description: String = "",
    val author: String = "",
    val homepage: String = "",
    val version: String = "",
    val allowShowUpdateAlert: Boolean = true,
)

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val createTime: Long,
    val musics: List<String> = emptyList(), // musicId 列表
    /** 在线列表的来源（平台歌单/榜单 id，空表示纯本地列表） */
    val source: String? = null,
    val sourceListId: String? = null,
    val locationUpdateTime: Long = 0,
)

/**
 * 列表内的一条歌曲。[id] 是本条记录 id（= music.id），[listId] 指明所属列表，
 * 因为歌曲按 `playlist_$listId.json` 分文件存放，导出备份时需要它还原归属。
 */
@Serializable
data class PlaylistMusic(
    val id: String,
    val music: com.lingxu.music.core.model.MusicInfo,
    val sort: Int,
    val listId: String = "",
)

class AppDatabase(private val context: Context) {

    companion object {
        private val JSON = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
        private const val FILE_API_SOURCE_LIST = "api_source_list.json"
        private const val FILE_PLAYLIST = "playlist.json"
        private const val FILE_FAVORITES = "favorites.json"
        private const val FILE_SEARCH_HISTORY = "search_history.json"
        private const val FILE_LAST_PLAY = "last_play.json"

        fun build(context: Context): AppDatabase = AppDatabase(context)
    }

    private val dataDir = context.filesDir

    // ============ 音源脚本 ============

    private val _apiSources = MutableStateFlow<List<ApiSourceMeta>>(emptyList())
    val apiSources: Flow<List<ApiSourceMeta>> = _apiSources.asStateFlow()

    private var apiSourcesLoaded = false

    suspend fun loadApiSources(): List<ApiSourceMeta> = withContext(Dispatchers.IO) {
        if (apiSourcesLoaded) return@withContext _apiSources.value
        val file = java.io.File(dataDir, FILE_API_SOURCE_LIST)
        val list = if (file.exists()) {
            runCatching {
                JSON.decodeFromString<List<ApiSourceMeta>>(file.readText())
            }.getOrElse { emptyList() }
        } else emptyList()
        apiSourcesLoaded = true
        _apiSources.value = list
        list
    }

    suspend fun getAllApiSources(): List<ApiSourceMeta> {
        if (!apiSourcesLoaded) return loadApiSources()
        return _apiSources.value
    }

    suspend fun getApiSource(id: String): ApiSourceMeta? =
        getAllApiSources().find { it.id == id }

    suspend fun upsertApiSource(meta: ApiSourceMeta) = withContext(Dispatchers.IO) {
        val list = getAllApiSources().toMutableList()
        val index = list.indexOfFirst { it.id == meta.id }
        if (index >= 0) list[index] = meta else list.add(meta)
        persistApiSources(list)
        _apiSources.value = list
    }

    suspend fun deleteApiSource(id: String) = withContext(Dispatchers.IO) {
        val list = getAllApiSources().filterNot { it.id == id }
        persistApiSources(list)
        _apiSources.value = list
        java.io.File(dataDir, "api_source_$id.js").delete()
    }

    suspend fun getApiSourceScript(id: String): String? = withContext(Dispatchers.IO) {
        val file = java.io.File(dataDir, "api_source_$id.js")
        if (file.exists()) file.readText() else null
    }

    suspend fun saveApiSourceScript(id: String, script: String) = withContext(Dispatchers.IO) {
        java.io.File(dataDir, "api_source_$id.js").writeAtomicText(script)
    }

    private fun persistApiSources(list: List<ApiSourceMeta>) {
        java.io.File(dataDir, FILE_API_SOURCE_LIST)
            .writeAtomicText(JSON.encodeToString(list))
    }

    // ============ 歌单 ============

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: Flow<List<Playlist>> = _playlists.asStateFlow()

    private var playlistsLoaded = false

    suspend fun loadPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        if (playlistsLoaded) return@withContext _playlists.value
        val file = java.io.File(dataDir, FILE_PLAYLIST)
        val list = if (file.exists()) {
            runCatching { JSON.decodeFromString<List<Playlist>>(file.readText()) }
                .getOrElse { emptyList() }
        } else emptyList()
        playlistsLoaded = true
        _playlists.value = list
        list
    }

    suspend fun upsertPlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        val list = loadPlaylists().toMutableList()
        val index = list.indexOfFirst { it.id == playlist.id }
        if (index >= 0) list[index] = playlist else list.add(playlist)
        persistPlaylists(list)
        _playlists.value = list
    }

    suspend fun deletePlaylist(id: String) = withContext(Dispatchers.IO) {
        val list = loadPlaylists().filterNot { it.id == id }
        persistPlaylists(list)
        _playlists.value = list
    }

    suspend fun getPlaylistMusics(listId: String): List<PlaylistMusic> = withContext(Dispatchers.IO) {
        val file = java.io.File(dataDir, "playlist_$listId.json")
        if (file.exists()) {
            runCatching { JSON.decodeFromString<List<PlaylistMusic>>(file.readText()) }
                .getOrElse { emptyList() }
        } else emptyList()
    }

    suspend fun savePlaylistMusics(listId: String, musics: List<PlaylistMusic>) =
        withContext(Dispatchers.IO) {
            java.io.File(dataDir, "playlist_$listId.json")
                .writeAtomicText(JSON.encodeToString(musics))
        }

    suspend fun deletePlaylistMusics(listId: String) = withContext(Dispatchers.IO) {
        java.io.File(dataDir, "playlist_$listId.json").delete()
        Unit
    }

    private fun persistPlaylists(list: List<Playlist>) {
        java.io.File(dataDir, FILE_PLAYLIST).writeAtomicText(JSON.encodeToString(list))
    }

    // ============ 我喜欢（收藏） ============

    suspend fun loadFavorites(): List<com.lingxu.music.core.model.MusicInfo> =
        withContext(Dispatchers.IO) {
            val file = java.io.File(dataDir, FILE_FAVORITES)
            if (file.exists()) {
                runCatching {
                    JSON.decodeFromString<List<com.lingxu.music.core.model.MusicInfo>>(file.readText())
                }.getOrElse { emptyList() }
            } else emptyList()
        }

    suspend fun saveFavorites(list: List<com.lingxu.music.core.model.MusicInfo>) =
        withContext(Dispatchers.IO) {
            java.io.File(dataDir, FILE_FAVORITES).writeAtomicText(JSON.encodeToString(list))
        }

    // ============ 搜索历史 ============

    suspend fun loadSearchHistory(): List<String> = withContext(Dispatchers.IO) {
        val file = java.io.File(dataDir, FILE_SEARCH_HISTORY)
        if (file.exists()) {
            runCatching { JSON.decodeFromString<List<String>>(file.readText()) }.getOrElse { emptyList() }
        } else emptyList()
    }

    suspend fun saveSearchHistory(list: List<String>) = withContext(Dispatchers.IO) {
        java.io.File(dataDir, FILE_SEARCH_HISTORY).writeAtomicText(JSON.encodeToString(list))
    }

    // ============ 上次播放（启动自动播放/保存进度用） ============

    suspend fun loadLastPlay(): com.lingxu.music.core.player.PlayState? =
        withContext(Dispatchers.IO) {
            val file = java.io.File(dataDir, FILE_LAST_PLAY)
            if (!file.exists()) return@withContext null
            runCatching { JSON.decodeFromString<com.lingxu.music.core.player.PlayState>(file.readText()) }
                .getOrNull()
        }

    suspend fun saveLastPlay(state: com.lingxu.music.core.player.PlayState) =
        withContext(Dispatchers.IO) {
            java.io.File(dataDir, FILE_LAST_PLAY)
                .writeAtomicText(JSON.encodeToString(state))
        }

    fun toInfo(meta: ApiSourceMeta): ApiSourceInfo = ApiSourceInfo(
        id = meta.id,
        name = meta.name,
        description = meta.description,
        author = meta.author,
        homepage = meta.homepage,
        version = meta.version,
        allowShowUpdateAlert = meta.allowShowUpdateAlert,
    )
}


/**
 * 原子写：先写 .tmp 再 rename。原先直接 writeText 是先截断再写，
 * 写盘期间被杀进程/断电会留下半个 JSON，而读取端解析失败会静默当成空数据并标记已加载，
 * 之后任何一次写操作都会把损坏文件覆盖成「空 + 新数据」，用户数据无声丢失。
 */
private fun java.io.File.writeAtomicText(text: String) {
    val tmp = java.io.File(parentFile, "$name.tmp")
    tmp.writeText(text)
    if (!tmp.renameTo(this)) {
        writeText(text)
        tmp.delete()
    }
}
