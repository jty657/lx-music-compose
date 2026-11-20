package com.lingxu.music.core.repository

import com.lingxu.music.core.request.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 应用内更新检查（对齐原版第六节的「版本更新源」机制）。
 *
 * 原版通过 version.json 宣告最新版本与下载地址。本项目作为移植版，沿用同一协议，
 * 但更新源指向项目自身的发布渠道；多个源按顺序 fallback，任一成功即返回。
 */
@Serializable
data class AppVersion(
    val versionName: String = "",
    val versionCode: Int = 0,
    val downloadUrl: String = "",
    val changelog: String = "",
)

class UpdateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /** 更新源列表：优先项目自身 GitHub Releases，其次兼容原版 version.json 协议。 */
    private val sources = listOf(
        "https://api.github.com/repos/jty657/lx-music-compose/releases/latest",
    )

    /**
     * 拉取最新版本信息。任一源解析成功即返回，全部失败返回 null。
     * GitHub Releases 的 JSON 与本项目 AppVersion 字段不同，这里做一次归一化。
     */
    suspend fun checkLatest(): AppVersion? = withContext(Dispatchers.IO) {
        for (url in sources) {
            val result = runCatching {
                val resp = HttpClient.instance.fetch(
                    url,
                    com.lingxu.music.core.request.ScriptRequestOptions(
                        headers = mapOf("Accept" to "application/vnd.github+json"),
                    ),
                )
                if (resp.statusCode !in 200..299) return@runCatching null
                val body = resp.body as? String ?: return@runCatching null
                parse(body)
            }
            val version = result.getOrNull()
            if (version != null && version.versionName.isNotBlank()) return@withContext version
        }
        null
    }

    /** 解析版本源 JSON，兼容 GitHub Releases 与 version.json 两种格式。 */
    private fun parse(body: String): AppVersion? {
        val obj = runCatching {
            json.parseToJsonElement(body) as? kotlinx.serialization.json.JsonObject
        }.getOrNull() ?: return null

        fun str(vararg keys: String): String {
            for (k in keys) {
                val v = obj[k] ?: continue
                val s = runCatching {
                    (v as? kotlinx.serialization.json.JsonPrimitive)?.content
                }.getOrNull() ?: continue
                if (s.isNotBlank() && s != "null") return s
            }
            return ""
        }
        fun intOf(vararg keys: String): Int {
            for (k in keys) {
                val v = obj[k] ?: continue
                val n = (v as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
                if (n != null) return n
            }
            return 0
        }

        // GitHub Releases: tag_name / name / html_url / body
        val name = str("versionName", "version", "tag_name", "name")
        val download = str("downloadUrl", "download_url", "html_url", "browser_download_url")
        if (name.isBlank()) return null
        return AppVersion(
            versionName = name.removePrefix("v").removePrefix("V"),
            versionCode = intOf("versionCode", "version_code"),
            downloadUrl = download,
            changelog = str("changelog", "body", "log"),
        )
    }

    companion object {
        /** 简单版本号比较：1.3.3 > 1.3.2。返回 1/0/-1。 */
        fun compareVersion(a: String, b: String): Int {
            val pa = a.removePrefix("v").removePrefix("V").split('.').map { it.toIntOrNull() ?: 0 }
            val pb = b.removePrefix("v").removePrefix("V").split('.').map { it.toIntOrNull() ?: 0 }
            val n = maxOf(pa.size, pb.size)
            for (i in 0 until n) {
                val x = pa.getOrElse(i) { 0 }
                val y = pb.getOrElse(i) { 0 }
                if (x > y) return 1
                if (x < y) return -1
            }
            return 0
        }
    }
}
