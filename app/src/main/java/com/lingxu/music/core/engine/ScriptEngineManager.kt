package com.lingxu.music.core.engine

import android.content.Context
import android.util.Log
import com.lingxu.music.core.engine.QuickJSEngine.EngineState
import com.lingxu.music.core.model.ApiSourceInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 音源引擎管理器。对应原版 core/userApi.ts + core/init/userApi/index.ts：
 * - 维护当前 QuickJS 引擎实例（切换音源时销毁重建）
 * - 维护 scriptRequestMap（脚本发起的外部 HTTP 请求）
 * - 维护 userApiRequestMap（App 发起的 musicUrl/lyric/pic 请求，20 秒超时）
 * - apiInitPromise 门控：取 URL/歌词/封面先等初始化完成
 */
class ScriptEngineManager(private val context: Context) {

    companion object {
        private const val TAG = "ScriptEngineManager"
        private const val REQUEST_TIMEOUT_MS = 20_000L
        private const val INIT_TIMEOUT_MS = 15_000L
    }

    private var engine: QuickJSEngine? = null
    private var currentApiId: String? = null

    /** 音源初始化状态（对齐原版 setUserApiStatus） */
    data class ApiStatus(
        val status: Boolean = false,
        val message: String = "",
    )

    private val _status = MutableStateFlow(ApiStatus(false, ""))
    val status: StateFlow<ApiStatus> = _status

    private var _initDeferred: CompletableDeferred<Boolean> = CompletableDeferred()
    private val _initInfo = MutableStateFlow<JsonObject?>(null)

    /** 当前音源声明的源能力：source -> actions/qualitys（对齐 global.lx.qualityList + apis） */
    private val _capabilities = MutableStateFlow<Map<String, JsonObject>>(emptyMap())
    val capabilities: StateFlow<Map<String, JsonObject>> = _capabilities

    /** 挂起的 App 请求（musicUrl/lyric/pic） */
    private val pendingAppRequests = ConcurrentHashMap<String, CompletableDeferred<JsonObject?>>()

    /** 脚本发起的 HTTP 请求（requestKey -> Job），原版 scriptRequestMap */
    private val scriptRequests = ConcurrentHashMap<String, kotlinx.coroutines.Job>()

    private val preloadScript: String by lazy {
        context.assets.open("scripts/user-api-preload.js").use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    }

    /** 预加载的默认能力（内置 SDK 源的 qualityList 由 SDK 自身提供，用户音源由脚本声明） */
    val builtinQualityList: Map<String, List<String>> = mapOf(
        "kw" to listOf("128k", "320k", "flac", "flac24bit"),
        "kg" to listOf("128k", "320k", "flac", "flac24bit"),
        "tx" to listOf("128k", "320k", "flac", "flac24bit"),
        "wy" to listOf("128k", "320k", "flac", "flac24bit"),
        "mg" to listOf("128k", "320k", "flac", "flac24bit"),
    )

    /** 加载并初始化自定义音源脚本（对应 setUserApi） */
    fun loadScript(info: ApiSourceInfo, script: String) {
        destroyScript()
        currentApiId = info.id
        _initDeferred = CompletableDeferred()
        _status.value = ApiStatus(false, "initing")
        _capabilities.value = emptyMap()

        val newEngine = QuickJSEngine(
            scriptInfo = QuickJSEngine.ScriptInfo(
                id = info.id,
                name = info.name,
                description = info.description,
                version = info.version,
                author = info.author,
                homepage = info.homepage,
            ),
            scriptText = script,
            preloadScript = preloadScript,
            onAction = { action ->
                when (action) {
                    is ScriptAction.Request -> {
                        // 脚本发起 HTTP 请求：直接由引擎内部执行
                        engine?.executeScriptRequest(action.requestKey, action.url, action.options)
                    }
                    is ScriptAction.CancelRequest -> {
                        scriptRequests.remove(action.requestKey)?.cancel()
                    }
                    is ScriptAction.Response -> {
                        // 脚本对 App 请求的响应：完成挂起的 Deferred
                        handleScriptResponse(
                            action.requestKey,
                            action.status,
                            action.errorMessage,
                            action.result,
                        )
                    }
                    is ScriptAction.Log -> {
                        Log.d(TAG, "[${action.level}] ${action.message}")
                    }
                    is ScriptAction.ShowUpdateAlert -> {
                        Log.i(TAG, "script update alert: ${action.log}")
                    }
                    is ScriptAction.Init -> Unit
                }
            },
            onState = { state ->
                when (state) {
                    is EngineState.Ready -> {
                        _status.value = ApiStatus(true, "")
                        _initInfo.value = state.initInfo
                        val caps = mutableMapOf<String, JsonObject>()
                        (state.initInfo?.get("sources") as? JsonObject)?.let { sources ->
                            sources.entries.forEach { (source, v) ->
                                val obj = v as? JsonObject
                                if (obj?.get("type")?.jsonPrimitive?.content == "music") {
                                    caps[source] = obj
                                }
                            }
                        }
                        _capabilities.value = caps
                        if (!_initDeferred.isCompleted) _initDeferred.complete(true)
                    }
                    is EngineState.Failed -> {
                        _status.value = ApiStatus(false, state.errorMessage)
                        if (!_initDeferred.isCompleted) _initDeferred.complete(false)
                    }
                    is EngineState.Loading -> Unit
                    is EngineState.Destroyed -> Unit
                }
            },
        )
        engine = newEngine
        newEngine.start()
    }

    /** 销毁脚本引擎（对应 destroyUserApi） */
    fun destroyScript() {
        engine?.destroy()
        engine = null
        currentApiId = null
        _capabilities.value = emptyMap()
        _status.value = ApiStatus(false, "")
        pendingAppRequests.values.forEach { it.completeExceptionally(Exception("api destroyed")) }
        pendingAppRequests.clear()
    }

    /** 等待当前音源初始化完成（对应 global.lx.apiInitPromise[0]） */
    suspend fun awaitInit(): Boolean {
        // 内置音源无需等待
        if (currentApiId == null) return true
        if (_status.value.status) return true
        // 脚本可能加载成功却从不 send(inited)；没有超时会让取链/歌词永久挂起
        return withTimeoutOrNull(INIT_TIMEOUT_MS) { _initDeferred.await() } ?: run {
            _status.value = ApiStatus(false, "音源初始化超时")
            false
        }
    }

    /** 源是否可用（对应 assertApiSupport） */
    fun isSourceSupported(source: String): Boolean {
        if (source == "local") return true
        if (currentApiId == null) return builtinQualityList.containsKey(source)
        return _capabilities.value.containsKey(source)
    }

    /** 获取源支持的音质列表 */
    fun getSourceQualitys(source: String): List<String> {
        if (currentApiId == null) return builtinQualityList[source] ?: emptyList()
        val cap = _capabilities.value[source] ?: return emptyList()
        return (cap.get("qualitys") as? kotlinx.serialization.json.JsonArray)
            ?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
    }

    /**
     * 请求音源脚本：musicUrl / lyric / pic
     * 返回 Promise 风格结果（对齐原版 handleUserApiResponse）
     */
    suspend fun requestScriptApi(
        source: String,
        action: String,
        info: JsonObject,
    ): JsonObject {
        awaitInit()
        val eng = engine ?: throw IllegalStateException("script not loaded")
        val requestKey = "request__${UUID.randomUUID()}"
        val deferred = CompletableDeferred<JsonObject?>()
        pendingAppRequests[requestKey] = deferred

        eng.request(source, action, info, requestKey)

        return try {
            val result = kotlinx.coroutines.withTimeout(REQUEST_TIMEOUT_MS) {
                deferred.await()
            } ?: throw IllegalStateException("no result")
            result
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            throw IllegalStateException("request timeout")
        } finally {
            pendingAppRequests.remove(requestKey)
        }
    }

    /** 引擎回调：脚本对 App 请求的响应（musicUrl/lyric/pic 结果） */
    fun handleScriptResponse(requestKey: String, status: Boolean, errorMessage: String?, result: JsonObject?) {
        val deferred = pendingAppRequests.remove(requestKey) ?: return
        if (status) {
            deferred.complete(result)
        } else {
            deferred.completeExceptionally(IllegalStateException(errorMessage ?: "failed"))
        }
    }

    /** 便捷方法：获取歌曲播放 URL（返回 url 字符串）；type 为 null 时对应原版 local 分支 */
    suspend fun getMusicUrl(source: String, musicInfoJson: JsonObject, type: String?): String {
        val result = requestScriptApi(
            source,
            "musicUrl",
            buildJsonObject {
                put("type", type?.let { JsonPrimitive(it) } ?: kotlinx.serialization.json.JsonNull)
                put("musicInfo", musicInfoJson)
            },
        )
        val data = result.get("data") as? JsonObject ?: throw IllegalStateException("invalid response")
        return data.get("url")?.jsonPrimitive?.content ?: throw IllegalStateException("invalid url")
    }

    /** 便捷方法：获取歌词 */
    suspend fun getLyric(source: String, musicInfoJson: JsonObject): JsonObject {
        val result = requestScriptApi(
            source,
            "lyric",
            buildJsonObject {
                put("type", kotlinx.serialization.json.JsonNull)
                put("musicInfo", musicInfoJson)
            },
        )
        return (result.get("data") as? JsonObject) ?: throw IllegalStateException("invalid lyric response")
    }

    /** 便捷方法：获取封面 */
    suspend fun getPic(source: String, musicInfoJson: JsonObject): String {
        val result = requestScriptApi(
            source,
            "pic",
            buildJsonObject {
                put("type", kotlinx.serialization.json.JsonNull)
                put("musicInfo", musicInfoJson)
            },
        )
        val data = result.get("data")
        return data?.jsonPrimitive?.content ?: throw IllegalStateException("invalid pic response")
    }

    fun currentApiId(): String? = currentApiId
}
