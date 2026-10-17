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

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

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
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("无法读取所选文件")
            val data = json.decodeFromString<BackupData>(text)
            if (data.version > BackupData.VERSION) {
                error("备份文件版本(v${data.version})比当前 App(v${BackupData.VERSION})更新，请先升级 App")
            }

            db.saveFavorites(data.favorites)

            // 覆盖式恢复：备份里不存在的列表要从本地删掉，否则「上次删掉的列表」会复活
            val keepIds = data.playlists.map { it.id }.toSet()
            db.loadPlaylists().filterNot { it.id in keepIds }.forEach {
                db.deletePlaylist(it.id)
                db.deletePlaylistMusics(it.id)
            }
            data.playlists.forEach { db.upsertPlaylist(it) }

            // PlaylistMusic.listId 指明归属；没有该字段的旧备份按「歌曲出现在哪个列表」无法反推，
            // 因此只恢复带 listId 的条目，其余跳过
            val grouped = data.playlistMusics
                .filter { it.listId.isNotBlank() }
                .groupBy { it.listId }
            // 逐列表覆盖写入（包括备份里为空列表的情况），否则该列表的旧歌会原样保留
            data.playlists.forEach { list ->
                db.savePlaylistMusics(list.id, grouped[list.id] ?: emptyList())
            }

            onImported?.invoke()

            data.playlists.size + data.playlistMusics.size
        }
    }

    /** SAF createDocument 用的默认文件名 */
    fun defaultFileName(): String = "$EXPORT_PREFIX${System.currentTimeMillis()}.json"

    fun exportMimeType(): String = MIME_JSON
}
