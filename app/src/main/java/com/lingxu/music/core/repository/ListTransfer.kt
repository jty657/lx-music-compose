package com.lingxu.music.core.repository

import android.content.Context
import android.net.Uri
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 单个歌单的分享文件（对应原版 Mylist 的「导入/导出」单项，playListPart 格式）。
 * 只含歌单名与歌曲列表，不含音源脚本等敏感数据。
 */
@Serializable
data class ListTransferData(
    val version: Int = VERSION,
    val name: String,
    val musics: List<MusicInfo> = emptyList(),
) {
    companion object {
        /** 结构变更时递增；读取方只接受 <= 自己的 version */
        const val VERSION = 1
    }
}

/** 单列表导入导出（SAF 文件读写） */
object ListTransfer {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /** 导出歌单到用户选定的 uri */
    suspend fun export(context: Context, uri: Uri, name: String, musics: List<MusicInfo>): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = ListTransferData(name = name, musics = musics)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.encodeToString(data).toByteArray())
                } ?: error("无法写入所选位置")
                musics.size
            }
        }

    /**
     * 从用户选定的 uri 读回歌单内容。
     *
     * 读出来会先做一遍原版 utils.filterMusicList 的等价过滤：丢掉无 id/无名的条目、按 id 去重、
     * 补上空 singer。导入的重复 id 会让列表页 LazyColumn 因重复 key 直接崩溃。
     */
    suspend fun read(context: Context, uri: Uri): Result<ListTransferData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                    ?: error("无法读取所选文件")
                val data = json.decodeFromString<ListTransferData>(text)
                if (data.version > ListTransferData.VERSION) error("文件版本过新，请升级 App")
                data.copy(musics = filterMusicList(data.musics))
            }
        }

    /** 过滤非法/重复条目，与原版 filterMusicList 对齐 */
    fun filterMusicList(list: List<MusicInfo>): List<MusicInfo> {
        val seen = HashSet<String>()
        return list.mapNotNull { music ->
            val id = music.id.trim()
            val name = music.name.trim()
            if (id.isEmpty() || name.isEmpty()) return@mapNotNull null
            if (!seen.add(id)) return@mapNotNull null
            // 旧文件/手工编辑可能带 null 或空白，统一补齐，避免后续逻辑拿到异常值
            music.copy(
                id = id,
                name = name,
                singer = music.singer.trim(),
            )
        }
    }
}
