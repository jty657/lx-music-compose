package com.lingxu.music.core.repository

import com.lingxu.music.core.db.ApiSourceMeta
import com.lingxu.music.core.db.AppDatabase
import com.lingxu.music.core.model.ApiSourceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 自定义音源仓库：对应原版 utils/data.ts 中 userApi 部分 */
class ApiSourceRepository(private val db: AppDatabase) {

    companion object {
        /** 与原版一致：最多 20 个自定义音源 */
        const val MAX_COUNT = 20
        /** 在线导入脚本大小上限（9MB） */
        const val MAX_SCRIPT_SIZE = 9_000_000
        /** 头部注释信息字段长度限制（原版 INFO_NAMES） */
        val INFO_LIMITS = mapOf(
            "name" to 24,
            "description" to 36,
            "author" to 56,
            "homepage" to 1024,
            "version" to 36,
        )
        /** 头部注释解析正则（原版 matchInfo） */
        private val INFO_RXP = Regex("^\\s?\\*\\s?@(\\w+)\\s(.+)$")
        /** 提取文件开头块注释（原版 addUserApi 的校验正则） */
        private val HEAD_COMMENT_RXP = Regex("^/\\*[\\S|\\s]+?\\*/")
    }

    val list: Flow<List<ApiSourceInfo>> = db.apiSources.map { metas ->
        metas.map { db.toInfo(it) }
    }

    suspend fun getAll(): List<ApiSourceInfo> = db.getAllApiSources().map { db.toInfo(it) }

    suspend fun getScript(id: String): String? = db.getApiSourceScript(id)

    suspend fun count(): Int = db.getAllApiSources().size

    /** 导入脚本（对应原版 addUserApi） */
    suspend fun importScript(script: String): ApiSourceInfo {
        val headComment = HEAD_COMMENT_RXP.find(script)?.value
            ?: throw IllegalArgumentException("无效的自定义源文件：缺少头部注释")

        val meta = matchInfo(headComment)
        val name = meta["name"].orEmpty().ifEmpty { "user_api_${System.currentTimeMillis()}" }
        val apiMeta = ApiSourceMeta(
            id = "user_api_${System.currentTimeMillis()}_${Thread.currentThread().id}",
            name = name,
            description = meta["description"].orEmpty(),
            author = meta["author"].orEmpty(),
            homepage = meta["homepage"].orEmpty(),
            version = meta["version"].orEmpty(),
            allowShowUpdateAlert = true,
        )
        db.upsertApiSource(apiMeta)
        db.saveApiSourceScript(apiMeta.id, script)
        return db.toInfo(apiMeta)
    }

    /** 删除音源（对应原版 removeUserApi） */
    suspend fun remove(ids: List<String>) {
        ids.forEach { id -> db.deleteApiSource(id) }
    }

    suspend fun setAllowShowUpdateAlert(id: String, enable: Boolean) {
        val meta = db.getApiSource(id) ?: return
        db.upsertApiSource(meta.copy(allowShowUpdateAlert = enable))
    }

    /** 解析脚本头部注释（对应原版 matchInfo） */
    private fun matchInfo(comment: String): Map<String, String> {
        val infos = mutableMapOf<String, String>()
        for (line in comment.split(Regex("\\r?\\n"))) {
            val result = INFO_RXP.find(line) ?: continue
            val key = result.groupValues[1]
            if (INFO_LIMITS.containsKey(key)) {
                infos[key] = result.groupValues[2].trim()
            }
        }
        return INFO_LIMITS.keys.associateWith { key ->
            val value = infos[key].orEmpty()
            val limit = INFO_LIMITS[key]!!
            if (value.length > limit) value.substring(0, limit) + "..." else value
        }
    }
}
