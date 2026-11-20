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

    /** 当前磁盘缓存占用（字节） */
    fun diskCacheSizeBytes(): Long = runCatching { client.cache?.size() ?: 0L }.getOrDefault(0L)

    /** 清空磁盘缓存（封面等）。只清空条目，不调用 delete()——后者会关闭底层缓存，导致之后所有缓存写入永久失效。 */
    fun clearDiskCache() {
        // 只清空条目，不调用 delete()：OkHttp 的 Cache.delete() 会关闭底层
        // DiskLruCache，此后读写缓存均返回 null，缓存被永久禁用且不可恢复。
        runCatching { client.cache?.evictAll() }
    }

    private val client: OkHttpClient by lazy { buildClient() }

    /** 按 readTimeout 毫秒缓存 client，复用同一 Dispatcher/ConnectionPool，避免每次请求新建 client */
    private val timeoutClients = java.util.concurrent.ConcurrentHashMap<Long, OkHttpClient>()

    private fun buildClient(readTimeoutMs: Long = 60_000L): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .apply {
                // 封面等可缓存资源走磁盘缓存，避免每次冷启动重下
                cacheDir?.let { cache(okhttp3.Cache(java.io.File(it, "http"), 50L * 1024 * 1024)) }
            }
            .build()

    /** 自定义超时 client 的容量上限：每个 client 带独立连接池/线程池，不能让脚本用递增 timeout 无限创建 */
    private val maxTimeoutClients = 8
    private val timeoutClientsLock = Any()

    /** 默认 client 之外，为自定义超时复用固定数量的 client（新建 client 会带独立连接池，故需缓存） */
    private fun clientForTimeout(timeoutMs: Long): OkHttpClient {
        if (timeoutMs == 60_000L) return client
        timeoutClients[timeoutMs]?.let { return it }
        // size 检查与 getOrPut 必须在同一把锁内完成，否则并发下多个线程同时判 size<8，
        // 各自插入不同档位会瞬时突破上限（低概率但确实存在的竞态）。
        return synchronized(timeoutClientsLock) {
            timeoutClients[timeoutMs]?.let { return@synchronized it }
            if (timeoutClients.size >= maxTimeoutClients) client
            else timeoutClients.getOrPut(timeoutMs) { buildClient(timeoutMs) }
        }
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
                method in BODY_METHODS -> ByteArray(0).toRequestBody(contentType?.toMediaType())
                else -> null
            }
            builder.method(method.uppercase(), body)

            // 下限 1ms 防 0/负值；上限 120s 防止脚本用超大 timeout 让请求长期挂住、
            // 占住协程与连接池（换源循环叠加时可能长时间无响应且无法取消）。
            val timeout = (options.timeout ?: 60_000).toLong().coerceIn(1L, 120_000L)
            val call = clientForTimeout(timeout).newCall(builder.build())

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
        // OkHttp 规定 PUT/PATCH 必须带 body；此前只处理 post，SDK 桥一旦用 put/patch 取链/签名
        // 会因 body 为空抛 IllegalArgumentException 而整次失败，且与异步 fetch() 行为不一致。
        val contentType = merged.entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value
            ?: "application/json"
        val requestBody = when {
            body != null -> body.toRequestBody(contentType.toMediaType())
            // 无显式 body 但方法要求带 body 时给空 body，避免 OkHttp 抛异常
            m in BODY_METHODS -> ByteArray(0).toRequestBody(contentType.toMediaType())
            else -> null
        }
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
        /** 需要携带 body 的 HTTP 方法（OkHttp 对 POST/PUT/PATCH 强制要求非 null body） */
        private val BODY_METHODS = setOf("post", "put", "patch")
    }
}
