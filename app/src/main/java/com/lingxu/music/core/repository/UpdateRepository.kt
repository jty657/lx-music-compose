package com.lingxu.music.core.repository

import com.lingxu.music.core.request.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 项目自身的发布渠道（更新检查只信任这里，避免被任意第三方改写）。 */
private const val REPO_API = "https://api.github.com/repos/jty657/lx-music-compose"
private const val REPO_WEB = "https://github.com/jty657/lx-music-compose"

/** 从提交信息里提取语义化版本号，如 "v1.4.2: 修复 …" → 1.4.2 */
private val VERSION_IN_MESSAGE = Regex("\\bv?(\\d+\\.\\d+\\.\\d+)\\b")

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

    /**
     * 更新源列表（按可信度排序）。
     *
     * 只查 `releases/latest` 曾导致「检查更新」永远失败——本仓库以 Actions 产物
     * 分发、并未发布 Release，该端点长期 404。这里补上标签与提交两条兜底链路，
     * 保证即使不发 Release 也能给出正确的「已是最新 / 有新版本」结论。
     */
    private val sources = listOf(
        "$REPO_API/releases/latest",
        "$REPO_API/releases?per_page=10",
        "$REPO_API/tags?per_page=10",
        "$REPO_API/commits/master",
    )

    /**
     * 拉取最新版本信息：并发友好地逐个尝试所有源，取其中**版本号最大**的一个。
     * 不能「第一个成功就返回」——Releases 列表的首项可能比标签旧，先命中会误报旧版本。
     * 全部失败返回 null。
     */
    suspend fun checkLatest(): AppVersion? = withContext(Dispatchers.IO) {
        var best: AppVersion? = null
        for (url in sources) {
            val one = runCatching {
                val resp = HttpClient.instance.fetch(
                    url,
                    com.lingxu.music.core.request.ScriptRequestOptions(
                        headers = mapOf("Accept" to "application/vnd.github+json"),
                    ),
                )
                if (resp.statusCode !in 200..299) return@runCatching null
                val body = resp.body as? String ?: return@runCatching null
                parse(body)
            }.getOrNull() ?: continue
            if (one.versionName.isBlank()) continue
            val currentBest = best
            if (currentBest == null || compareVersion(one.versionName, currentBest.versionName) > 0) {
                best = one
            }
        }
        best
    }

    /**
     * 解析版本源 JSON，兼容三类响应：
     * - GitHub Releases（单个对象或列表数组）
     * - GitHub Tags（数组，元素含 name）
     * - GitHub Commit（对象，版本号藏在 commit.message 里）
     * 以及原版 version.json 风格的对象。
     */
    private fun parse(body: String): AppVersion? {
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() ?: return null
        // 列表接口（releases/tags）返回数组：取首项作为该源的代表版本
        val obj = when (root) {
            is kotlinx.serialization.json.JsonObject -> root
            is kotlinx.serialization.json.JsonArray ->
                root.firstOrNull() as? kotlinx.serialization.json.JsonObject
            else -> null
        } ?: return null

        fun kotlinx.serialization.json.JsonElement.strOrNull(): String? =
            (this as? kotlinx.serialization.json.JsonPrimitive)?.content
                ?.takeIf { it.isNotBlank() && it != "null" }

        fun str(vararg keys: String): String {
            for (k in keys) {
                val s = obj[k]?.strOrNull() ?: continue
                return s
            }
            return ""
        }
        fun intOf(vararg keys: String): Int {
            for (k in keys) {
                val n = obj[k]?.strOrNull()?.toIntOrNull() ?: continue
                return n
            }
            return 0
        }

        // 提交信息（commits/master 没有版本字段，只能从 message 里取，如 "v1.4.2: …"）
        val commitMessage = (obj["commit"] as? kotlinx.serialization.json.JsonObject)
            ?.get("message")?.strOrNull().orEmpty()

        // Releases 的资产里优先取 .apk 直链，点开即下载安装包
        val apkUrl = (obj["assets"] as? kotlinx.serialization.json.JsonArray)
            ?.asSequence()
            ?.mapNotNull { it as? kotlinx.serialization.json.JsonObject }
            ?.firstOrNull { it["name"]?.strOrNull()?.endsWith(".apk", ignoreCase = true) == true }
            ?.get("browser_download_url")?.strOrNull()

        val name = str("versionName", "version_name", "version", "tag_name", "name")
            .ifBlank { VERSION_IN_MESSAGE.find(commitMessage)?.groupValues?.get(1).orEmpty() }
        if (name.isBlank()) return null

        // 下载地址优先取 Release 的 .apk 资产；commit 页（/commit/xxx）不是下载入口，需排除；
        // 再兜底到 Releases 页面，用户仍能手动进入下载。
        val htmlUrl = str("downloadUrl", "download_url", "browser_download_url", "html_url")
        val download = apkUrl?.takeIf { it.isNotBlank() }
            ?: htmlUrl.takeIf { it.isNotBlank() && !it.contains("/commit/") }
            ?: "$REPO_WEB/releases"
        return AppVersion(
            versionName = name.removePrefix("v").removePrefix("V"),
            versionCode = intOf("versionCode", "version_code"),
            downloadUrl = download,
            changelog = str("changelog", "body", "log").ifBlank { commitMessage },
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
