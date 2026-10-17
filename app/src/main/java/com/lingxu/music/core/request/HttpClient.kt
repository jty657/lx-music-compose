package com.lingxu.music.core.request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** 原版 user_api 脚本 lx.request 的响应 */
data class ScriptResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String>,
    val body: Any?,
)

/** 原版 RequestParams.options */
data class ScriptRequestOptions(
    val method: String = "get",
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val form: Map<String, String>? = null,
    val formData: Map<String, String>? = null,
    val binary: Boolean = false,
    val timeout: Int? = null,
)

/** 请求客户端：供音源脚本与内置 SDK 共用 */
class HttpClient {

    private val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36",
    )

    /** 磁盘缓存目录（由 App 注入；HttpClient 是单例，拿不到 Context） */
    @Volatile
    private var cacheDir: java.io.File? = null

    fun setCacheDir(dir: java.io.File) {
        cacheDir = dir
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .apply {
                // 封面等可缓存资源走磁盘缓存，避免每次冷启动重下
                cacheDir?.let { cache(okhttp3.Cache(java.io.File(it, "http"), 50L * 1024 * 1024)) }
            }
            .build()
    }

    suspend fun fetch(url: String, options: ScriptRequestOptions = ScriptRequestOptions()): ScriptResponse =
        withContext(Dispatchers.IO) {
            val method = options.method.lowercase()
            val merged = defaultHeaders + options.headers
            val builder = Request.Builder().url(url)
            // Content-Type 交给 RequestBody 管（否则会被 OkHttp 用 body 自带类型覆盖）
            merged.forEach { (k, v) -> if (!k.equals("Content-Type", ignoreCase = true)) builder.header(k, v) }
            val contentType = merged.entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value

            val body: okhttp3.RequestBody? = when {
                options.form != null -> {
                    val fb = FormBody.Builder()
                    options.form.forEach { (k, v) -> fb.add(k, v) }
                    fb.build()
                }
                options.formData != null -> {
                    val mb = MultipartBody.Builder().setType(MultipartBody.FORM)
                    options.formData.forEach { (k, v) -> mb.addFormDataPart(k, v) }
                    mb.build()
                }
                options.body != null -> {
                    val ct = (contentType ?: "application/json").toMediaType()
                    when (val b = options.body) {
                        is String -> b.toRequestBody(ct)
                        is ByteArray -> b.toRequestBody(ct)
                        // 桥把 JS 对象转成了 Map，Map.toString() 不是合法 JSON
                        is Map<*, *> -> org.json.JSONObject(b).toString().toRequestBody(ct)
                        else -> b.toString().toRequestBody(ct)
                    }
                }
                // OkHttp 要求 POST/PUT/PATCH 必须有 body
                method in setOf("post", "put", "patch") -> ByteArray(0).toRequestBody(contentType?.toMediaType())
                else -> null
            }
            builder.method(method.uppercase(), body)

            val timeout = options.timeout ?: 60_000
            val call = client.newBuilder()
                .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
                .build()
                .newCall(builder.build())

            call.execute().use { resp ->
                val bytes = resp.body?.bytes() ?: ByteArray(0)
                ScriptResponse(
                    statusCode = resp.code,
                    statusMessage = resp.message,
                    headers = resp.headers.toMultimap().mapValues { it.value.joinToString("; ") },
                    body = if (options.binary) bytes else String(bytes, Charsets.UTF_8),
                )
            }
        }

    /** 简单 GET 文本 */
    suspend fun fetchText(url: String): String =
        fetch(url).body as? String ?: ""

    /**
     * 同步请求（供 SDK 桥线程直接调用，阻塞式）。
     * @param binary true 时 body 为原始字节（ByteArray），否则为 UTF-8 字符串。
     */
    fun fetchSync(
        url: String,
        method: String = "get",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        binary: Boolean = false,
    ): ScriptResponse {
        val builder = Request.Builder().url(url)
        val merged = defaultHeaders + headers
        merged.forEach { (k, v) -> if (!k.equals("Content-Type", ignoreCase = true)) builder.header(k, v) }
        val m = method.lowercase()
        val requestBody = if (m == "post" && body != null) {
            val contentType = merged.entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value
                ?: "application/json"
            body.toRequestBody(contentType.toMediaType())
        } else null
        builder.method(m.uppercase(), requestBody)
        client.newCall(builder.build()).execute().use { resp ->
            val bytes = resp.body?.bytes() ?: ByteArray(0)
            return ScriptResponse(
                statusCode = resp.code,
                statusMessage = resp.message,
                headers = resp.headers.toMultimap().mapValues { it.value.joinToString("; ") },
                body = if (binary) bytes else String(bytes, Charsets.UTF_8),
            )
        }
    }

    /** 同步拉取二进制（封面图等；fetchSync 的 body 是 UTF-8 字符串，不能用于图片） */
    fun fetchBytesSync(url: String, headers: Map<String, String> = emptyMap()): ByteArray? {
        val builder = Request.Builder().url(url)
        (defaultHeaders + headers).forEach { (k, v) -> builder.header(k, v) }
        client.newCall(builder.build()).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.bytes()
        }
    }

    companion object {
        val instance by lazy { HttpClient() }
    }
}
