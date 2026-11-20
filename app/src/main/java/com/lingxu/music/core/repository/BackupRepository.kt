package com.lingxu.music.core.repository

import android.content.Context
import android.net.Uri
import com.lingxu.music.core.db.AppDatabase
import com.lingxu.music.core.db.Playlist
import com.lingxu.music.core.db.PlaylistMusic
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 备份仓库：把「我需要恢复的东西」打成一个 JSON 文件（对应原版 Sync/Backup）。
 *
 * 范围（对齐原版的「歌单」「我的列表」分项，**不含自定义音源脚本**，避免把用户的
 * 第三方音源凭据/密钥一起导出到用户可能分享出去的文件里）：
 * - 我喜欢 / 收藏
 * - 我的列表（含每首歌，因为歌曲信息是播放必需的最小集）
 *
 * 版本号用于向前兼容：老版本读到不认识的 VERSION 时应拒绝导入并提示，
 * 而不是写坏本地数据。
 */
@Serializable
data class BackupData(
    val version: Int = VERSION,
    val favorites: List<MusicInfo> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val playlistMusics: List<PlaylistMusic> = emptyList(),
) {
    companion object {
        /** 结构变更时递增；读取方只接受 <= 自己的 version */
        const val VERSION = 1
    }
}

class BackupRepository(
    private val context: Context,
    private val db: AppDatabase,
) {
    companion object {
        private const val MIME_JSON = "application/json"
        private const val EXPORT_PREFIX = "lingxu-backup-"
    }

    // encodeDefaults=true：既把 version 真正写进备份文件（否则版本检查形同虚设），
    // 也让空数据的备份带上所有数据键而非编码成 "{}"，导入时结构校验可据此区分合法空备份与损坏文件。
    // 旧备份无 version 字段时反序列化仍回落到默认值 VERSION，保持向后兼容。
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    /** 导入完成后的回调：让内存缓存失效重建（备份是直接写 db 的，不刷新会被旧缓存覆盖） */
    var onImported: (suspend () -> Unit)? = null

    /** 把当前数据写到用户选定的 uri（SAF 输出流），返回导出的条目数 */
    suspend fun export(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val favorites = db.loadFavorites()
            val playlists = db.loadPlaylists()
            val playlistMusics = playlists.flatMap { db.getPlaylistMusics(it.id) }
            val data = BackupData(
                favorites = favorites,
                playlists = playlists,
                playlistMusics = playlistMusics,
            )
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.encodeToString(data).toByteArray())
            } ?: error("无法写入所选位置")
            favorites.size + playlistMusics.size
        }
    }

    /**
     * 从用户选定的 uri 读回并覆盖本地数据。
     * @return 恢复的列表数与歌曲数
     */
    suspend fun import(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        val result = runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("无法读取所选文件")
            // 结构校验（decode 前）：kotlinx 会把 "{}" 解码成全默认值的 BackupData
            // （version=1），原先用 data.version<=0 判断损坏几乎永不成立，导致空/损坏文件
            // 被当成合法备份导入、清空用户收藏与歌单。先要求顶层是 JSON 对象且含数据键之一。
            val root = runCatching { json.parseToJsonElement(text) }.getOrNull()
                as? kotlinx.serialization.json.JsonObject
                ?: error("备份文件内容为空或已损坏，未做任何改动")
            val knownKeys = setOf("favorites", "playlists", "playlistMusics")
            if (root.keys.none { it in knownKeys }) {
                error("备份文件内容为空或已损坏，未做任何改动")
            }
            val data = json.decodeFromString<BackupData>(text)
            // 第二道判据：三类数据全空视为无内容可恢复，拒绝导入（有内容的备份不可能三者皆空）。
            // 老版本备份未开 encodeDefaults、本就不含 version 字段，故不能靠缺 version 判定损坏。
            if (data.favorites.isEmpty() && data.playlists.isEmpty() && data.playlistMusics.isEmpty()) {
                error("备份文件内容为空或已损坏，未做任何改动")
            }
            if (data.version > BackupData.VERSION) {
                error("备份文件版本(v${data.version})比当前 App(v${BackupData.VERSION})更新，请先升级 App")
            }

            // 过滤非法/重复条目（对齐原版 filterMusicList），否则导入的重复 id 会让列表页崩溃；
            // 同时把 listId 做白名单校验，备份里的 id 会直接拼进文件名，不能含路径分隔符。
            val favorites = ListTransfer.filterMusicList(data.favorites)
            val playlists = data.playlists.filter { it.id.matches(SAFE_ID) }
            val playlistMusics = data.playlistMusics
                .filter { it.listId.matches(SAFE_ID) && it.id.matches(SAFE_ID) }

            db.saveFavorites(favorites)

            // 覆盖式恢复：备份里不存在的列表要从本地删掉，否则「上次删掉的列表」会复活。
            // 旧写法 deletePlaylist + N 次 upsertPlaylist 每条都全量重写 playlist.json（O(N) 次写盘），
            // 改为只删这些列表的歌曲文件，再用一次 replacePlaylists 覆盖整表（O(1) 次写盘，语义等价）。
            val keepIds = playlists.map { it.id }.toSet()
            db.loadPlaylists().filterNot { it.id in keepIds }.forEach {
                db.deletePlaylistMusics(it.id)
            }
            db.replacePlaylists(playlists)

            // PlaylistMusic.listId 指明归属；没有该字段的旧备份按「歌曲出现在哪个列表」无法反推，
            // 因此只恢复带 listId 的条目，其余跳过
            val grouped = playlistMusics
                .filter { it.listId.isNotBlank() }
                .groupBy { it.listId }
            // 逐列表覆盖写入（包括备份里为空列表的情况），否则该列表的旧歌会原样保留
            playlists.forEach { list ->
                db.savePlaylistMusics(list.id, grouped[list.id] ?: emptyList())
            }

            playlists.size + playlistMusics.size
        }
        // 失败时磁盘可能已经是「半恢复」状态：无论成败都重建内存缓存，
        // 否则下一次写操作会用旧缓存把刚恢复（或部分恢复）的数据整表覆盖掉
        runCatching { onImported?.invoke() }
        result
    }

    /** 列表/歌曲 id 白名单：这些 id 会直接拼进文件名，必须挡住路径分隔符 */
    private val SAFE_ID = Regex("^[A-Za-z0-9._-]+$")

    /** SAF createDocument 用的默认文件名 */
    fun defaultFileName(): String = "$EXPORT_PREFIX${System.currentTimeMillis()}.json"

    fun exportMimeType(): String = MIME_JSON
}
