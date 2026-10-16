package com.lxmusic.mobile.core.engine

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.lxmusic.mobile.core.crypto.CryptoUtils
import com.lxmusic.mobile.core.model.MusicInfo
import com.lxmusic.mobile.core.request.HttpClient
import com.lxmusic.mobile.core.request.ScriptRequestOptions
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 音源脚本执行结果。与原版 RN 侧 ResponseParams 对齐：
 * status: 是否成功；errorMessage: 失败原因；result: musicUrl/lyric/pic 的原始结果
 */
data class ApiActionResult(
    val status: Boolean,
    val errorMessage: String? = null,
    val result: JsonObject? = null,
)

/** 音源脚本事件（对齐原版 ActionsEvent） */
sealed class ScriptAction {
    data class Init(val status: Boolean, val errorMessage: String?, val info: JsonObject?) : ScriptAction()
    data class Request(
        val requestKey: String,
        val url: String,
        val options: ScriptRequestOptions,
    ) : ScriptAction()
    data class CancelRequest(val requestKey: String) : ScriptAction()
    data class Response(
        val requestKey: String,
        val status: Boolean,
        val errorMessage: String?,
        val result: JsonObject?,
    ) : ScriptAction()
    data class Log(val level: String, val message: String) : ScriptAction()
    data class ShowUpdateAlert(val log: String, val updateUrl: String?) : ScriptAction()
}

/**
 * QuickJS 音源脚本引擎。1:1 移植原版 QuickJS.java + JavaScriptThread.java：
 * - 专用 HandlerThread 执行 JS（QuickJSContext 同线程要求）
 * - 注入与原版一致的 6 个 native 函数与 __lx_native_call__ 桥
 * - 使用与原版相同的 user-api-preload.js（assets/scripts/user-api-preload.js）
 * - nativeCall 事件通过 actionHandler 回调到 Kotlin 侧
 */
class QuickJSEngine(
    private val scriptInfo: ScriptInfo,
    private val scriptText: String,
    private val preloadScript: String,
    private val onAction: (ScriptAction) -> Unit,
    private val onState: (EngineState) -> Unit,
) {
    data class ScriptInfo(
        val id: String,
        val name: String,
        val description: String,
        val version: String,
        val author: String,
        val homepage: String,
    )

    sealed class EngineState {
        object Loading : EngineState()
        data class Ready(val initInfo: JsonObject?) : EngineState()
        data class Failed(val errorMessage: String) : EngineState()
        object Destroyed : EngineState()
    }

    companion object {
        private const val TAG = "QuickJSEngine"
        private const val MSG_INIT = 1
        private const val MSG_ACTION = 2
        private const val MSG_DESTROY = 3

        @Volatile
        private var loaderInited = false

        private fun ensureLoader() {
            if (!loaderInited) synchronized(QuickJSEngine::class.java) {
                if (!loaderInited) {
                    QuickJSLoader.init()
                    loaderInited = true
                }
            }
        }
    }

    private val key = UUID.randomUUID().toString()
    private var jsContext: QuickJSContext? = null
    private var inited = false
    private val thread = HandlerThread("JS-${scriptInfo.name}-${System.currentTimeMillis()}")
    private var handler: Handler? = null
    private val timeoutHandler = Handler(android.os.Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingRequests = ConcurrentHashMap<String, Job>()
    private val json = Json { ignoreUnknownKeys = true }

    /** 启动引擎线程并加载脚本 */
    fun start() {
        thread.start()
        handler = object : Handler(thread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_INIT -> handleInit()
                    MSG_ACTION -> {
                        val (action, data) = msg.obj as Pair<*, *>
                        callJS(action as String, data as? String)
                    }
                    MSG_DESTROY -> {
                        jsContext?.destroy()
                        jsContext = null
                        thread.quitSafely()
                        onState(EngineState.Destroyed)
                    }
                }
            }
        }
        handler!!.sendEmptyMessage(MSG_INIT)
    }

    private fun handleInit() {
        try {
            ensureLoader()
            val context = QuickJSContext.create()
            jsContext = context
            context.setConsole(object : QuickJSContext.Console {
                override fun log(info: String) = onAction(ScriptAction.Log("log", info))
                override fun info(info: String) = onAction(ScriptAction.Log("info", info))
                override fun warn(info: String) = onAction(ScriptAction.Log("warn", info))
                override fun error(info: String) = onAction(ScriptAction.Log("error", info))
            })
            createEnvObj(context)
            context.evaluate(preloadScript)
            context.getGlobalObject().getJSFunction("lx_setup").call(
                key,
                scriptInfo.id,
                scriptInfo.name,
                scriptInfo.description,
                scriptInfo.version,
                scriptInfo.author,
                scriptInfo.homepage,
                scriptText,
            )
            onState(EngineState.Loading)
            try {
                context.evaluate(scriptText)
            } catch (e: Exception) {
                Log.e(TAG, "load script error: ${e.message}")
                runCatching { callJS("__run_error__") }
                if (inited) return
                inited = true
                onState(EngineState.Failed(e.message ?: "script load failed"))
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "create js env failed: ${e.message}")
            onState(EngineState.Failed(e.message ?: "create JavaScript Env failed"))
        }
    }

    /** 与原版 createEnvObj 一致的 native 函数注入 */
    private fun createEnvObj(context: QuickJSContext) {
        val global = context.getGlobalObject()
        global.setProperty("__lx_native_call__") { args: Array<Any?> ->
            if (key == args[0]) {
                callNative(args[1] as String, args[2] as String)
            }
            null
        }
        global.setProperty("__lx_native_call__utils_str2b64") { args: Array<Any?> ->
            runCatching { CryptoUtils.str2b64(args[0] as String) }.getOrElse {
                Log.e(TAG, "utils_str2b64 error: ${it.message}")
                ""
            }
        }
        global.setProperty("__lx_native_call__utils_b642buf") { args: Array<Any?> ->
            runCatching {
                val bytes = CryptoUtils.b642buf(args[0] as String)
                bytes.joinToString(prefix = "[", postfix = "]") { (it.toInt() and 0xFF).toString() }
            }.getOrElse {
                Log.e(TAG, "utils_b642buf error: ${it.message}")
                ""
            }
        }
        global.setProperty("__lx_native_call__utils_str2md5") { args: Array<Any?> ->
            runCatching { CryptoUtils.str2md5(args[0] as String) }.getOrElse {
                Log.e(TAG, "utils_str2md5 error: ${it.message}")
                ""
            }
        }
        global.setProperty("__lx_native_call__utils_aes_encrypt") { args: Array<Any?> ->
            runCatching {
                CryptoUtils.aesEncrypt(
                    args[0] as String,
                    args[1] as String,
                    args[2] as String,
                    args[3] as String,
                )
            }.getOrElse {
                Log.e(TAG, "utils_aes_encrypt error: ${it.message}")
                ""
            }
        }
        global.setProperty("__lx_native_call__utils_rsa_encrypt") { args: Array<Any?> ->
            runCatching {
                CryptoUtils.rsaEncrypt(args[0] as String, args[1] as String, args[2] as String)
            }.getOrElse {
                Log.e(TAG, "utils_rsa_encrypt error: ${it.message}")
                ""
            }
        }
        global.setProperty("__lx_native_call__set_timeout") { args: Array<Any?> ->
            val id = args[0].toString()
            val timeout = (args[1] as Number).toInt()
            timeoutHandler.postDelayed({ callJS("__set_timeout__", id) }, timeout.toLong())
            null
        }
    }

    /** 脚本侧 nativeCall 回调入口（在 JS 线程执行，需转主线程发事件） */
    private fun callNative(action: String, data: String) {
        Log.d(TAG, "script call action: $action")
        timeoutHandler.post { handleNativeAction(action, data) }
    }

    /** 在主线程处理脚本事件（避免与 JS 线程抢锁） */
    private fun handleNativeAction(action: String, data: String) {
        val parsed = runCatching { json.parseToJsonElement(data) }.getOrNull()
        when (action) {
            "init" -> {
                val obj = parsed as? JsonObject
                val status = obj?.get("status")?.jsonPrimitive?.boolean ?: false
                if (status && !inited) {
                    inited = true
                    onState(EngineState.Ready(obj.get("info") as? JsonObject))
                } else if (!status) {
                    onState(EngineState.Failed(obj?.get("errorMessage")?.jsonPrimitive?.content ?: "init failed"))
                }
            }
            "request" -> {
                val obj = parsed as? JsonObject ?: return
                val requestKey = obj.get("requestKey")?.jsonPrimitive?.content ?: return
                val url = obj.get("url")?.jsonPrimitive?.content ?: return
                val optionsObj = obj.get("options") as? JsonObject ?: JsonObject(emptyMap())
                val options = ScriptRequestOptions(
                    method = optionsObj.get("method")?.jsonPrimitive?.content ?: "get",
                    headers = (optionsObj.get("headers") as? JsonObject)?.let { h ->
                        h.entries.associate { (k, v) -> k to (v.jsonPrimitive.content) }
                    } ?: emptyMap(),
                    body = optionsObj.get("body")?.let { jsonElementToAny(it) },
                    form = (optionsObj.get("form") as? JsonObject)?.let { f ->
                        f.entries.associate { (k, v) -> k to v.jsonPrimitive.content }
                    },
                    formData = (optionsObj.get("formData") as? JsonObject)?.let { f ->
                        f.entries.associate { (k, v) -> k to v.jsonPrimitive.content }
                    },
                    binary = optionsObj.get("binary")?.jsonPrimitive?.boolean ?: false,
                    timeout = optionsObj.get("timeout")?.jsonPrimitive?.int,
                )
                // 通知外部执行 HTTP 请求
                onAction(ScriptAction.Request(requestKey, url, options))
            }
            "cancelRequest" -> {
                val requestKey = parsed?.jsonPrimitive?.content ?: return
                pendingRequests.remove(requestKey)?.cancel()
                onAction(ScriptAction.CancelRequest(requestKey))
            }
            "response" -> {
                // 脚本对 App 请求（musicUrl/lyric/pic）的响应
                val obj = parsed as? JsonObject ?: return
                val requestKey = obj.get("requestKey")?.jsonPrimitive?.content ?: return
                val status = obj.get("status")?.jsonPrimitive?.boolean ?: false
                val errorMessage = obj.get("errorMessage")?.jsonPrimitive?.content
                val result = obj.get("result") as? JsonObject
                onAction(ScriptAction.Response(requestKey, status, errorMessage, result))
            }
            "showUpdateAlert" -> {
                val obj = parsed as? JsonObject ?: return
                val log = obj.get("log")?.jsonPrimitive?.content ?: return
                val updateUrl = obj.get("updateUrl")?.jsonPrimitive?.content
                onAction(ScriptAction.ShowUpdateAlert(log, updateUrl))
            }
            "log" -> {
                val obj = parsed as? JsonObject ?: return
                val level = obj.get("level")?.jsonPrimitive?.content ?: "info"
                val message = obj.get("message")?.jsonPrimitive?.content ?: ""
                onAction(ScriptAction.Log(level, message))
            }
            else -> Log.w(TAG, "Unknown native action: $action")
        }
    }

    /** 响应外部 HTTP 请求结果给脚本 */
    fun sendResponse(requestKey: String, error: String?, response: JsonObject?) {
        postAction(
            "response",
            buildJsonObject {
                put("requestKey", JsonPrimitive(requestKey))
                if (error != null) {
                    put("error", JsonPrimitive(error))
                } else {
                    put("error", JsonNull)
                }
                if (response != null) {
                    put("response", response)
                } else {
                    put("response", JsonNull)
                }
            }.toString(),
        )
    }

    /** 供外部请求音源脚本（musicUrl/lyric/pic） */
    fun request(source: String, action: String, info: JsonObject, requestKey: String) {
        postAction(
            "request",
            buildJsonObject {
                put("requestKey", JsonPrimitive(requestKey))
                put("data", buildJsonObject {
                    put("source", JsonPrimitive(source))
                    put("action", JsonPrimitive(action))
                    put("info", info)
                })
            }.toString(),
        )
    }

    /** 外部响应脚本的 HTTP 请求（由 ScriptAction.Request 触发后调用） */
    fun executeScriptRequest(requestKey: String, url: String, options: ScriptRequestOptions) {
        val job = scope.launch {
            runCatching {
                HttpClient.instance.fetch(url, options)
            }.onSuccess { resp ->
                val bodyJson: JsonElement = when (val b = resp.body) {
                    null -> JsonNull
                    is String -> runCatching { json.parseToJsonElement(b) }.getOrElse { JsonPrimitive(b) }
                    is ByteArray -> JsonNull
                    else -> JsonPrimitive(b.toString())
                }
                sendResponse(
                    requestKey,
                    null,
                    buildJsonObject {
                        put("statusCode", JsonPrimitive(resp.statusCode))
                        put("statusMessage", JsonPrimitive(resp.statusMessage))
                        put("headers", buildJsonObject {
                            resp.headers.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
                        })
                        put("body", bodyJson)
                    },
                )
            }.onFailure { e ->
                sendResponse(requestKey, e.message ?: "request failed", null)
            }
        }
        pendingRequests[requestKey] = job
    }

    /** 销毁引擎 */
    fun destroy() {
        handler?.sendEmptyMessage(MSG_DESTROY)
        pendingRequests.values.forEach { it.cancel() }
        pendingRequests.clear()
    }

    fun isReady() = inited

    private fun postAction(action: String, data: String?) {
        handler?.obtainMessage(MSG_ACTION, Pair(action, data))?.sendToTarget()
    }

    /** 在 JS 线程执行（供 nativeCall 桥与事件分发） */
    private fun callJS(action: String, data: String? = null) {
        val context = jsContext ?: return
        try {
            val fn = context.getGlobalObject().getJSFunction("__lx_native__")
            if (data == null) {
                fn.call(key, action)
            } else {
                fn.call(key, action, data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Call script error: ${e.message}")
            var msg = e.message ?: "unknown error"
            if (msg.length > 1024) msg = msg.substring(0, 1024) + "..."
            onAction(ScriptAction.Log("error", "Call script error: $msg"))
            if (!inited) {
                onState(EngineState.Failed(msg))
                inited = true
            }
        }
    }

    private fun jsonElementToAny(el: JsonElement): Any? = when (el) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            el.isString -> el.content
            el.booleanOrNull != null -> el.booleanOrNull ?: false
            el.intOrNull != null -> el.intOrNull ?: 0
            el.doubleOrNull != null -> el.doubleOrNull ?: 0.0
            else -> el.content
        }
        is JsonArray -> el.map { jsonElementToAny(it) }
        is JsonObject -> el.entries.associate { (k, v) -> k to jsonElementToAny(v) }
    }
}

/** musicInfo 转为旧版脚本结构（原版 toOldMusicInfo） */
fun MusicInfo.toOldMusicInfo(): JsonObject = buildJsonObject {
    put("name", JsonPrimitive(name))
    put("singer", JsonPrimitive(singer))
    put("source", JsonPrimitive(source))
    put("songmid", JsonPrimitive(meta.songId))
    put("interval", interval?.let { JsonPrimitive(it) } ?: JsonNull)
    put("albumName", JsonPrimitive(meta.albumName))
    put("img", JsonPrimitive(meta.picUrl ?: ""))
    put("typeUrl", buildJsonObject {})
    if (source == "local") {
        put("filePath", JsonPrimitive(meta.filePath ?: ""))
        put("ext", JsonPrimitive(meta.ext ?: ""))
        put("albumId", JsonPrimitive(""))
        put("types", buildJsonObject {})
        put("_types", buildJsonObject {})
    } else {
        put("albumId", meta.albumId?.let { JsonPrimitive(it) } ?: JsonNull)
        put("types", buildJsonArray {
            qualitys.forEach { q ->
                add(buildJsonObject {
                    put("type", JsonPrimitive(q.type))
                    put("size", q.size?.let { JsonPrimitive(it) } ?: JsonNull)
                    q.hash?.let { put("hash", JsonPrimitive(it)) }
                })
            }
        })
        put("_types", buildJsonObject {
            _qualitys.forEach { (k, v) ->
                put(k, buildJsonObject {
                    put("size", v.size?.let { JsonPrimitive(it) } ?: JsonNull)
                    v.hash?.let { put("hash", JsonPrimitive(it)) }
                })
            }
        })
        when (source) {
            "kg" -> hash?.let { put("hash", JsonPrimitive(it)) }
            "tx" -> {
                strMediaMid?.let { put("strMediaMid", JsonPrimitive(it)) }
                put("songId", JsonPrimitive(meta.songId))
            }
            "mg" -> {
                copyrightId?.let { put("copyrightId", JsonPrimitive(it)) }
                lrcUrl?.let { put("lrcUrl", JsonPrimitive(it)) }
                mrcUrl?.let { put("mrcUrl", JsonPrimitive(it)) }
                trcUrl?.let { put("trcUrl", JsonPrimitive(it)) }
            }
        }
    }
}

/** 旧版脚本结构转 MusicInfo（原版 toNewMusicInfo） */
fun JsonObject.toNewMusicInfo(): MusicInfo {
    val source = get("source")?.jsonPrimitive?.content ?: ""
    val songmid = get("songmid")?.jsonPrimitive?.content ?: ""
    val qualityList = mutableListOf<com.lxmusic.mobile.core.model.MusicQuality>()
    val qualityMap = mutableMapOf<String, com.lxmusic.mobile.core.model.QualitySize>()
    (get("types") as? JsonObject)?.let { types ->
        types.entries.forEach { (type, v) ->
            val obj = v as? JsonObject
            qualityList.add(
                com.lxmusic.mobile.core.model.MusicQuality(
                    type = type,
                    size = obj?.get("size")?.jsonPrimitive?.content,
                    hash = obj?.get("hash")?.jsonPrimitive?.content,
                ),
            )
        }
    }
    (get("_types") as? JsonObject)?.let { types ->
        types.entries.forEach { (type, v) ->
            val obj = v as? JsonObject
            qualityMap[type] = com.lxmusic.mobile.core.model.QualitySize(
                size = obj?.get("size")?.jsonPrimitive?.content,
                hash = obj?.get("hash")?.jsonPrimitive?.content,
            )
        }
    }
    return MusicInfo(
        id = "${source}_$songmid",
        name = get("name")?.jsonPrimitive?.content ?: "",
        singer = get("singer")?.jsonPrimitive?.content ?: "",
        source = source,
        interval = get("interval")?.jsonPrimitive?.content,
        meta = com.lxmusic.mobile.core.model.MusicMeta(
            songId = songmid,
            albumName = get("albumName")?.jsonPrimitive?.content ?: "",
            picUrl = (get("img")?.jsonPrimitive?.content ?: "").ifEmpty { null },
            albumId = get("albumId")?.jsonPrimitive?.content,
            filePath = get("filePath")?.jsonPrimitive?.content,
            ext = get("ext")?.jsonPrimitive?.content,
        ),
        qualitys = qualityList,
        _qualitys = qualityMap,
        hash = get("hash")?.jsonPrimitive?.content,
        strMediaMid = get("strMediaMid")?.jsonPrimitive?.content,
        copyrightId = get("copyrightId")?.jsonPrimitive?.content,
        lrcUrl = get("lrcUrl")?.jsonPrimitive?.content,
        mrcUrl = get("mrcUrl")?.jsonPrimitive?.content,
        trcUrl = get("trcUrl")?.jsonPrimitive?.content,
    )
}
