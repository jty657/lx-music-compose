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

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetch(url: String, options: ScriptRequestOptions = ScriptRequestOptions()): ScriptResponse =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder().url(url)
            val headers = defaultHeaders + options.headers
            headers.forEach { (k, v) -> builder.header(k, v) }

            val method = options.method.lowercase()
            var body: okhttp3.RequestBody? = null
            when {
                method == "post" && options.form != null -> {
                    val fb = FormBody.Builder()
                    options.form.forEach { (k, v) -> fb.add(k, v) }
                    body = fb.build()
                }
                method == "post" && options.formData != null -> {
                    val mb = MultipartBody.Builder().setType(MultipartBody.FORM)
                    options.formData.forEach { (k, v) -> mb.addFormDataPart(k, v) }
                    body = mb.build()
                }
                method == "post" && options.body != null -> {
                    body = when (val b = options.body) {
                        is String -> b.toRequestBody("application/json".toMediaType())
                        is ByteArray -> b.toRequestBody(null)
                        else -> b.toString().toRequestBody("application/json".toMediaType())
                    }
                }
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

    /** 同步请求（供 SDK 桥线程直接调用，阻塞式） */
    fun fetchSync(
        url: String,
        method: String = "get",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
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
                body = String(bytes, Charsets.UTF_8),
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
