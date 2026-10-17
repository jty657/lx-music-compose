package com.lingxu.music.core.engine

import android.content.Context
import android.util.Log
import com.lingxu.music.core.crypto.CryptoUtils
import com.lingxu.music.core.model.Board
import com.lingxu.music.core.model.BoardDetail
import com.lingxu.music.core.model.LyricInfo
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SongListDetail
import com.lingxu.music.core.model.SongListInfo
import com.lingxu.music.core.model.SongListPage
import com.lingxu.music.core.model.SongListTag
import com.lingxu.music.core.model.SongListTagGroup
import com.lingxu.music.core.model.SongListTagInfo
import com.lingxu.music.core.request.HttpClient
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * 内置官方音源 SDK（kw/kg/tx/wy/mg）桥接器。
 * 在 QuickJS 上下文中执行打包好的 music-sdk.bundle.js（原版 src/utils/musicSdk 的 esbuild 产物），
 * 通过 __lxNative 提供 md5/aes/rsa/btoa 等原生能力（替代原版 react-native-quick-md5 等 RN 模块）。
 *
 * 与原版一致：内置 SDK 负责 search/lyric/pic/leaderboard/hotSearch/getMusicUrl；
 * 其中 getMusicUrl 当全局 apiSource 为 user_api_xxx 时走 global.lx.apis（由外部注入）。
 */
class BuiltinSdkBridge(context: Context) {

    companion object {
        private const val TAG = "BuiltinSdkBridge"
        private const val TIMEOUT_MS = 20_000L

        /**
         * JS 侧 fetch 包装：原生桥只回传 id 与结果，避免把 JS 函数作为参数持到原生侧
         * （JS 函数传到 Java 会被包成 JSFunction，不是 JSCallFunction，无法事后调用）。
         * resp.text()/json()/blob() 都是引擎原生 Promise。
         */
        private val fetchShim = """
            globalThis.__lxFetchCbs = {};
            globalThis.__lxFetchSeq = 0;
            globalThis.__lxB64ToBytes = function (b64) {
              var bin = atob(b64 || '');
              var out = new Uint8Array(bin.length);
              for (var i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
              return out;
            };
            globalThis.__lxB64ToUtf8 = function (b64) {
              var bin = atob(b64 || '');
              var out = '';
              var i = 0;
              while (i < bin.length) {
                var c = bin.charCodeAt(i++);
                if (c < 0x80) out += String.fromCharCode(c);
                else if (c < 0xE0) out += String.fromCharCode(((c & 0x1F) << 6) | (bin.charCodeAt(i++) & 0x3F));
                else if (c < 0xF0) out += String.fromCharCode(((c & 0x0F) << 12) | ((bin.charCodeAt(i++) & 0x3F) << 6) | (bin.charCodeAt(i++) & 0x3F));
                else {
                  var cp = ((c & 0x07) << 18) | ((bin.charCodeAt(i++) & 0x3F) << 12) | ((bin.charCodeAt(i++) & 0x3F) << 6) | (bin.charCodeAt(i++) & 0x3F);
                  cp -= 0x10000;
                  out += String.fromCharCode(0xD800 + (cp >> 10), 0xDC00 + (cp & 0x3FF));
                }
              }
              return out;
            };
            globalThis.__lxFetchDone = function (id, ok, status, statusText, b64, headersJson) {
              var cb = __lxFetchCbs[id];
              if (!cb) return;
              delete __lxFetchCbs[id];
              cb(ok, status, statusText, b64, headersJson);
            };
            // SDK 的二进制分支（blobToBuffer）用 FileReader.readAsDataURL(blob) 取 base64，QuickJS 无此全局对象
            globalThis.FileReader = function () { this.result = null; this.onload = null; this.onerror = null; };
            globalThis.FileReader.prototype.readAsDataURL = function (blob) {
              try {
                this.result = 'data:application/octet-stream;base64,' + ((blob && blob.__b64) || '');
                if (this.onload) this.onload();
              } catch (e) { if (this.onerror) this.onerror(e); }
            };
            globalThis.fetch = function (url, options) {
              options = options || {};
              var o = {
                method: options.method || 'get',
                headers: options.headers || {},
                body: options.body == null ? null : String(options.body)
              };
              return new Promise(function (resolve, reject) {
                var id = ++__lxFetchSeq;
                __lxFetchCbs[id] = function (ok, status, statusText, b64, headersJson) {
                  if (!ok) { reject(new Error(status || 'fetch failed')); return; }
                  var map = {};
                  try { map = JSON.parse(headersJson || '{}'); } catch (e) {}
                  resolve({
                    status: status,
                    statusCode: status,
                    statusText: statusText || '',
                    ok: status >= 200 && status < 300,
                    url: url,
                    get body() { return __lxB64ToUtf8(b64); },
                    headers: { map: map },
                    text: function () { return Promise.resolve(__lxB64ToUtf8(b64)); },
                    json: function () {
                      try { return Promise.resolve(JSON.parse(__lxB64ToUtf8(b64))); }
                      catch (e) { return Promise.reject(e); }
                    },
                    blob: function () { return Promise.resolve({ __b64: b64, size: __lxB64ToBytes(b64).length, type: '' }); }
                  });
                };
                __lxFetch(id, url, JSON.stringify(o));
              });
            };
            // QuickJS 没有 setTimeout/clearTimeout；bundle 的 fetchData 依赖它做请求超时，
            // 缺了会让每一次 SDK 请求直接抛 ReferenceError。
            globalThis.__lxTimers = {};
            globalThis.__lxTimerSeq = 0;
            globalThis.__lxTimerFire = function (id, repeat) {
              var fn = __lxTimers[id];
              if (!fn) return;
              if (!repeat) delete __lxTimers[id];
              try { fn(); } catch (e) {}
            };
            globalThis.setTimeout = function (fn, ms) {
              var id = ++__lxTimerSeq;
              __lxTimers[id] = fn;
              __lxTimer(id, Number(ms) || 0);
              return id;
            };
            globalThis.clearTimeout = function (id) {
              delete __lxTimers[id];
              __lxTimerCancel(id);
            };
            globalThis.setInterval = function (fn, ms) {
              var id = ++__lxTimerSeq;
              __lxTimers[id] = fn;
              __lxTimerInterval(id, Number(ms) || 0);
              return id;
            };
            globalThis.clearInterval = function (id) {
              delete __lxTimers[id];
              __lxTimerCancel(id);
            };
        """.trimIndent()

        @Volatile
        private var loaderInited = false
    }

    // 两个 assets 共 ~587KB：属性初始化器会在构造函数（Application.onCreate，主线程）里同步读盘+inflate+解码。
    // 改为 lazy，首次访问发生在 init() 的 SDK 线程上，不再阻塞首帧。
    private val sdkBundle: String by lazy {
        context.assets.open("engine/music-sdk.bundle.js").use {
            it.readBytes().toString(Charsets.UTF_8)
        }
    }

    /**
     * Buffer polyfill。bundle 的 esbuild 配置是 platform:neutral，不会注入全局 Buffer，
     * 而 QuickJS 也没有 Node 的 Buffer，不先注入会报 "'Buffer' is not defined" 导致 SDK 初始化失败。
     */
    private val bufferPolyfill: String by lazy {
        context.assets.open("engine/buffer-polyfill.js").use {
            it.readBytes().toString(Charsets.UTF_8)
        }
    }

    /** 单线程执行器：QuickJSContext 同线程要求 */
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "BuiltinSDK").apply { isDaemon = true }
    }

    /** SDK 内 setTimeout/setInterval 的调度器；回调统一回到 SDK 线程执行 */
    private val timerExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "BuiltinSDKTimer").apply { isDaemon = true }
    }
    private val timers = ConcurrentHashMap<Int, ScheduledFuture<*>>()

    private var jsContext: QuickJSContext? = null
    private var sdkObject: JSObject? = null

    private val json = Json { ignoreUnknownKeys = true }

    /** 当前全局 apiSource（user_api_xxx 时 SDK 的 apis() 会走 global.lx.apis） */
    @Volatile
    var currentApiSource: String = ""

    /** 初始化 SDK 上下文 */
    fun init(apiSource: String) {
        currentApiSource = apiSource
        runOnSdkThread {
            try {
                synchronized(Companion) {
                    if (!loaderInited) {
                        QuickJSLoader.init()
                        loaderInited = true
                    }
                }
                val context = QuickJSContext.create()
                jsContext = context
                // 必须接上 console：wrapper 内置的 console.js 未指定 stdout 时，任何 console.log 都会抛
                // "When invoke console stuff, you should be set a stdout of platform to console.stdout."；
                // SDK bundle 的 fetchData 会 console.log，导致所有搜索失败。
                context.setConsole(object : QuickJSContext.Console {
                    override fun log(info: String) { Log.i(TAG, "[sdk] $info") }
                    override fun info(info: String) { Log.i(TAG, "[sdk] $info") }
                    override fun warn(info: String) { Log.w(TAG, "[sdk] $info") }
                    override fun error(info: String) { Log.e(TAG, "[sdk] $info") }
                })
                val global = context.getGlobalObject()

                // 注入 native 能力（SDK 桩函数通过 __lxNative 调用）
                val nativeObj = context.createNewJSObject()
                nativeObj.setProperty("md5") { args: Array<Any?> ->
                    runCatching { CryptoUtils.md5(args[0] as String) }.getOrElse { "" }
                }
                nativeObj.setProperty("btoa") { args: Array<Any?> ->
                    runCatching {
                        CryptoUtils.str2b64(args[0] as String)
                    }.getOrElse { "" }
                }
                nativeObj.setProperty("aesEncrypt") { args: Array<Any?> ->
                    runCatching {
                        CryptoUtils.aesEncrypt(
                            args[0] as String,
                            args[1] as String,
                            args[2] as String,
                            args[3] as String,
                        )
                    }.getOrElse { "" }
                }
                nativeObj.setProperty("aesDecrypt") { args: Array<Any?> ->
                    runCatching {
                        CryptoUtils.aesDecrypt(
                            args[0] as String,
                            args[1] as String,
                            args[2] as String,
                            args[3] as String,
                        )
                    }.getOrElse { "" }
                }
                nativeObj.setProperty("rsaEncrypt") { args: Array<Any?> ->
                    runCatching {
                        CryptoUtils.rsaEncrypt(args[0] as String, args[1] as String, args[2] as String)
                    }.getOrElse { "" }
                }
                nativeObj.setProperty("sha1") { args: Array<Any?> ->
                    runCatching { CryptoUtils.sha1(args[0] as String) }.getOrElse { "" }
                }
                global.setProperty("__lxNative", nativeObj)

                // 注入 global.lx（SDK 的 api-source.js 用 global.lx.apis）
                val lxObj = context.createNewJSObject()
                lxObj.setProperty("qualityList", context.createNewJSObject())
                lxObj.setProperty("apis", context.createNewJSObject())
                lxObj.setProperty("env", "mobile")
                lxObj.setProperty("version", "2.0.0")
                global.setProperty("lx", lxObj)

                // 注入 global.__lxApiSource（setting-state 桩读取）
                global.setProperty("__lxApiSource", currentApiSource)

                // process 桩：SDK 需要签名的请求（如 tx）会读 process.versions.app，缺失会导致该源整源失败
                val processObj = context.createNewJSObject()
                val processVersions = context.createNewJSObject()
                processVersions.setProperty("app", "1.9.1")
                processObj.setProperty("versions", processVersions)
                processObj.setProperty("env", context.createNewJSObject())
                processObj.setProperty("platform", "android")
                global.setProperty("process", processObj)

                // AbortController 桩（fetch 的 signal 参数需要）
                global.setProperty("AbortController") { _: Array<Any?> ->
                    val ctrl = context.createNewJSObject()
                    ctrl.setProperty("signal", context.createNewJSObject())
                    ctrl.setProperty("abort") { _: Array<Any?> -> null }
                    ctrl
                }
                // 原生 HTTP 桥 + JS 侧用引擎原生 Promise 包装。
                // SDK 的 request.js 会对 global.fetch(...).then(...) 做多级链式，自造 thenable 语义不完整，
                // 这里在 JS 里包裹成真 Promise，resp.text()/json()/blob() 也都是真 Promise。
                global.setProperty("__lxFetch") { args: Array<Any?> ->
                    val id = (args.getOrNull(0) as? Number)?.toInt()
                    val url = args.getOrNull(1) as? String
                    val optionsJson = args.getOrNull(2) as? String ?: "{}"
                    if (id != null && url != null) startFetch(id, url, optionsJson)
                    null
                }
                context.evaluate(fetchShim, "lx-fetch-shim.js")
                // 定时器桥（见 fetchShim 里的 setTimeout 实现）
                global.setProperty("__lxTimer") { args: Array<Any?> -> scheduleTimer(args, repeat = false) }
                global.setProperty("__lxTimerInterval") { args: Array<Any?> -> scheduleTimer(args, repeat = true) }
                global.setProperty("__lxTimerCancel") { args: Array<Any?> -> cancelTimer(args) }
                // i18n 桩：bundle 只用它拼「x分钟前」这类相对时间文本
                val i18nObj = context.createNewJSObject()
                i18nObj.setProperty("t") { args: Array<Any?> ->
                    val key = args.getOrNull(0) as? String ?: ""
                    val num = (args.getOrNull(1) as? JSObject)?.getIntProperty("num") ?: 0
                    when {
                        key.endsWith("second") -> "${num}秒前"
                        key.endsWith("minute") -> "${num}分钟前"
                        key.endsWith("hour") -> "${num}小时前"
                        else -> ""
                    }
                }
                global.setProperty("i18n", i18nObj)

                // 执行 Buffer polyfill（必须早于 SDK bundle）
                context.evaluate(bufferPolyfill, "buffer-polyfill.js")

                // 执行 SDK bundle
                context.evaluate(sdkBundle, "music-sdk.bundle.js")
                val lxMusicSdk = global.getJSObject("LXMusicSDK")
                sdkObject = lxMusicSdk?.getJSObject("default") ?: lxMusicSdk
                Log.i(TAG, "SDK loaded: ${sdkObject != null}")
            } catch (e: Exception) {
                Log.e(TAG, "SDK init failed: ${e.message}", e)
                jsContext = null
                sdkObject = null
            }
        }
    }

    /** 更新当前音源（user_api 切换时 SDK 行为变化） */
    fun setApiSource(apiSource: String) {
        currentApiSource = apiSource
        runOnSdkThread {
            jsContext?.getGlobalObject()?.setProperty("__lxApiSource", apiSource)
            // global.lx.apis 由外部注入用户音源的适配层
        }
    }

    /** 注入用户音源适配层（对应原版 init/userApi/index.ts 中的 apis 包装） */
    fun injectUserApis(apis: Map<String, JSObject>) {
        runOnSdkThread {
            val context = jsContext ?: return@runOnSdkThread
            val lxObj = context.getGlobalObject().getJSObject("lx") ?: return@runOnSdkThread
            val apisObj = lxObj.getJSObject("apis") ?: return@runOnSdkThread
            apis.forEach { (source, api) -> apisObj.setProperty(source, api) }
        }
    }

    fun isReady(): Boolean = sdkObject != null

    fun destroy() {
        runOnSdkThread {
            jsContext?.destroy()
            jsContext = null
            sdkObject = null
        }
        executor.shutdown()
    }

    // ================= 搜索 =================

    /** 搜索（对应原版 musicSdk[source].musicSearch.search） */
    suspend fun search(
        text: String,
        page: Int,
        source: String,
        limit: Int = 30,
    ): SearchResult {
        if (source == "all") {
            // 并发搜索全部内置源（逐个跑时最坏要等 5×单源超时）
            val sources = listOf("kw", "kg", "tx", "wy", "mg")
            val results = kotlinx.coroutines.coroutineScope {
                sources.map { s ->
                    async {
                        runCatching { search(text, page, s, limit) }
                            .getOrElse {
                                SearchResult(1, limit, s, emptyList(), 0)
                            }
                    }
                }.awaitAll()
            }
            val merged = results.flatMap { it.list }
            return SearchResult(
                allPage = results.maxOf { it.allPage },
                limit = limit,
                source = "all",
                list = merged,
                total = results.sumOf { it.total },
            )
        }
        val promise = withSdkThread {
            val module = getModule(source) ?: throw IllegalStateException("source not found: $source")
            val musicSearch = module.getJSObject("musicSearch") ?: throw IllegalStateException("musicSearch not found")
            val fn = musicSearch.getJSFunction("search")
            asPromise(callJSFunction(fn, arrayOf(text, page, limit)))
        }
        val result = awaitPromise(promise)
        return withSdkThread { parseSearchResult(result, source) }
    }

    // ================= 榜单 =================

    suspend fun getBoardsList(source: String): List<Board> {
        val promise = withSdkThread {
            val module = getModule(source) ?: return@withSdkThread null
            val leaderboard = module.getJSObject("leaderboard") ?: return@withSdkThread null
            val fn = leaderboard.getJSFunction("getBoards") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf()))
        } ?: return emptyList()
        val result = awaitPromise(promise)
        return withSdkThread { parseBoards(result, source) }
    }

    suspend fun getBoardList(source: String, bangId: String, page: Int): BoardDetail {
        val promise = withSdkThread {
            val module = getModule(source) ?: return@withSdkThread null
            val leaderboard = module.getJSObject("leaderboard") ?: return@withSdkThread null
            val fn = leaderboard.getJSFunction("getList") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf(bangId, page)))
        } ?: return BoardDetail("${source}__$bangId", "")
        val result = awaitPromise(promise)
        return withSdkThread { parseBoardDetail(result, source, bangId) }
    }

    // ================= 歌单 =================

    /** 歌单排序选项（对齐原版 songlist sortList） */
    fun getSongListSorts(source: String): List<Pair<String, String>> = when (source) {
        "kw", "tx" -> listOf("推荐" to "5", "最热" to "6", "最新" to "7", "热藏" to "3", "飙升" to "8")
        "kg", "mg" -> listOf("推荐" to "recommend", "最热" to "hot", "最新" to "new", "热藏" to "hot_collect", "飙升" to "rise")
        "wy" -> listOf("推荐" to "hot", "最新" to "new")
        else -> emptyList()
    }

    /** 歌单标签（各源统一走 songList.getTags） */
    suspend fun getSongListTags(source: String): SongListTagInfo {
        val promise = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getTags") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf()))
        } ?: return SongListTagInfo(source = source)
        val result = awaitPromise(promise)
        return withSdkThread { parseSongListTags(result, source) }
    }

    /** 歌单列表 */
    suspend fun getSongList(source: String, sortId: String, tagId: String, page: Int): SongListPage {
        val promise = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getList") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf(sortId, tagId, page)))
        } ?: return SongListPage(source = source, sortId = sortId, tagId = tagId)
        val result = awaitPromise(promise)
        return withSdkThread { parseSongListPage(result, source, sortId, tagId) }
    }

    /** 歌单详情（歌单内歌曲） */
    suspend fun getSongListDetail(source: String, id: String, page: Int): SongListDetail {
        val promise = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getListDetail") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf(id, page)))
        } ?: return SongListDetail(source = source, id = id)
        val result = awaitPromise(promise)
        return withSdkThread { parseSongListDetail(result, source, id) }
    }

    /** 搜索歌单（对齐原版 core/search/songlist.ts） */
    suspend fun searchSongList(text: String, page: Int, source: String, limit: Int = 18): SongListPage {
        val promise = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("search") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf(text, page, limit)))
        } ?: return SongListPage(source = source)
        val result = awaitPromise(promise)
        return withSdkThread { parseSongListPage(result, source, "", "") }
    }

    /** 歌单搜索聚合（all = 并发各源） */
    suspend fun searchSongListAll(text: String, page: Int, limit: Int = 15): SongListPage {
        val sources = listOf("kw", "kg", "tx", "wy", "mg")
        val results = sources.map { s ->
            runCatching { searchSongList(text, page, s, limit) }
                .getOrElse { SongListPage(source = s) }
        }
        return SongListPage(
            list = results.flatMap { it.list }.distinctBy { "${it.source}_${it.id}" },
            total = results.sumOf { it.total },
            page = page,
            limit = limit,
            maxPage = results.maxOf { it.maxPage },
            source = "all",
        )
    }

    /**
     * 归一化 SDK 返回值：SDK 多数方法返回 { promise } 或直接返回 Promise/thenable
     */
    private fun asPromise(raw: Any?): JSObject {
        val obj = raw as? JSObject ?: throw IllegalStateException("invalid sdk response")
        return obj.getJSObject("promise") ?: obj
    }

    private fun getSongListModule(source: String): JSObject? =
        getModule(source)?.getJSObject("songList")

    // ================= 热搜 =================

    suspend fun getHotSearch(source: String): List<String> {
        val promise = withSdkThread {
            val module = getModule(source) ?: return@withSdkThread null
            val hotSearch = module.getJSObject("hotSearch") ?: return@withSdkThread null
            val fn = hotSearch.getJSFunction("getList") ?: return@withSdkThread null
            asPromise(callJSFunction(fn, arrayOf()))
        } ?: return emptyList()
        val result = awaitPromise(promise)
        return withSdkThread { parseHotSearch(result, source) }
    }

    // ================= 歌词 =================

    suspend fun getLyric(source: String, oldInfo: JsonObject): LyricInfo {
        val jsObj = withSdkThread {
            val module = getModule(source) ?: throw IllegalStateException("source not found: $source")
            val fn = module.getJSFunction("getLyric")
            val raw = callJSFunction(fn, arrayOf(oldInfo.toJsObject()))
            raw as? JSObject ?: throw IllegalStateException("invalid lyric response")
        }
        // 返回 { promise } 结构
        val promise = withSdkThread { jsObj.getJSObject("promise") }
            ?: throw IllegalStateException("lyric promise missing")
        val result = awaitPromise(promise) as? JSObject ?: throw IllegalStateException("lyric failed")
        return withSdkThread { parseLyric(result) }
    }

    // ================= 封面 =================

    suspend fun getPic(source: String, oldInfo: JsonObject): String {
        val raw = withSdkThread {
            val module = getModule(source) ?: throw IllegalStateException("source not found: $source")
            val fn = module.getJSFunction("getPic")
            callJSFunction(fn, arrayOf(oldInfo.toJsObject()))
        }
        // 返回 url 字符串或 Promise / { promise }
        return when (raw) {
            is String -> raw
            else -> {
                val jsObj = raw as? JSObject ?: throw IllegalStateException("invalid pic response")
                val promise = withSdkThread { jsObj.getJSObject("promise") ?: jsObj }
                val result = awaitPromise(promise)
                when (result) {
                    is String -> result
                    else -> withSdkThread { (result as? JSObject)?.getString("url") }
                        ?: result?.toString()
                        ?: throw IllegalStateException("pic failed")
                }
            }
        }
    }

    // ================= 歌曲 URL（内置源无 api-list 时走 global.lx.apis） =================

    suspend fun getMusicUrl(source: String, oldInfo: JsonObject, type: String): String {
        val jsObj = withSdkThread {
            val module = getModule(source) ?: throw IllegalStateException("source not found: $source")
            val fn = module.getJSFunction("getMusicUrl")
            callJSFunction(fn, arrayOf(oldInfo.toJsObject(), type)) as? JSObject
                ?: throw IllegalStateException("invalid response")
        }
        // 返回 { promise } 或 { canceleFn, promise }
        val promise = withSdkThread { jsObj.getJSObject("promise") }
            ?: throw IllegalStateException("Api is not found")
        val result = awaitPromise(promise) as? JSObject ?: throw IllegalStateException("music url failed")
        return withSdkThread { result.getString("url") }
            ?: throw IllegalStateException("invalid url")
    }

    /**
     * 播放音质选择（对应原版 core/music/utils.ts getPlayQuality）：
     * 仅在偏好为高音质档位时逐级下降，否则固定 128k
     */
    fun getPlayQuality(preferQuality: String, musicInfo: MusicInfo, qualityList: List<String>): String {
        val tryQualitys = listOf("flac24bit", "flac", "320k")
        if (preferQuality !in tryQualitys) return "128k"
        return tryQualitys
            .dropWhile { it != preferQuality }
            .firstOrNull { q -> musicInfo._qualitys.containsKey(q) && qualityList.contains(q) }
            ?: "128k"
    }

    // ================= 内部实现 =================

    data class SearchResult(
        val allPage: Int,
        val limit: Int,
        val source: String,
        val list: List<MusicInfo>,
        val total: Int,
    )

    private fun getModule(source: String): JSObject? = sdkObject?.getJSObject(source)

    private fun runOnSdkThread(block: () -> Unit) {
        executor.execute(block)
    }

    /** 在 SDK 线程执行并挂起等待结果 */
    private suspend fun <T> withSdkThread(block: () -> T): T =
        suspendCancellableCoroutine { cont ->
            executor.execute {
                val result = runCatching { block() }
                if (cont.isActive) {
                    result.onSuccess { cont.resume(it) }
                        .onFailure { cont.resumeWith(Result.failure(it)) }
                }
            }
        }

    private fun callJSFunction(fn: com.whl.quickjs.wrapper.JSFunction, args: Array<Any?>): Any? {
        return fn.call(*args)
    }

    private suspend fun <T> withSdkContext(block: () -> T): T =
        withContext(Dispatchers.IO) { block() }

    /** 等待 JS Promise（在 SDK 线程注册回调） */
    private suspend fun awaitPromise(promise: JSObject): Any? =
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                executor.execute {
                    val then = promise.getJSFunction("then")
                    if (then != null) {
                        then.call(
                            { args: Array<Any?> ->
                                if (cont.isActive) cont.resume(args.getOrNull(0))
                            },
                            { args: Array<Any?> ->
                                if (cont.isActive) {
                                    val err = args.getOrNull(0)
                                    cont.resumeWith(Result.failure(Exception(err?.toString() ?: "promise rejected")))
                                }
                            },
                        )
                    } else {
                        if (cont.isActive) cont.resume(promise)
                    }
                }
            }
        } ?: throw IllegalStateException("promise timeout")

    /** 把 JSON 对象转为 JS 对象传入 SDK */
    private fun JsonObject.toJsObject(): JSObject {
        val context = jsContext ?: throw IllegalStateException("SDK not ready")
        val obj = context.createNewJSObject()
        entries.forEach { (k, v) ->
            when (v) {
                is JsonNull -> Unit
                is JsonPrimitive -> {
                    if (v.isString) obj.setProperty(k, v.content)
                    else v.booleanOrNull?.let { obj.setProperty(k, it) }
                        ?: v.intOrNull?.let { obj.setProperty(k, it) }
                        ?: v.doubleOrNull?.let { obj.setProperty(k, it) }
                }
                is JsonArray -> {
                    val arr = context.createNewJSArray()
                    v.forEachIndexed { i, el ->
                        jsonElementToJava(el, context)?.let { context.set(arr, it, i) }
                    }
                    obj.setProperty(k, arr)
                }
                is JsonObject -> obj.setProperty(k, v.toJsObject())
            }
        }
        return obj
    }

    private fun jsonElementToJava(el: JsonElement, context: QuickJSContext): Any? = when (el) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            el.isString -> el.content
            el.booleanOrNull != null -> el.booleanOrNull ?: false
            el.intOrNull != null -> el.intOrNull ?: 0
            el.doubleOrNull != null -> el.doubleOrNull ?: 0.0
            else -> el.content
        }
        is JsonArray -> el.map { jsonElementToJava(it, context) }
        is JsonObject -> el.toJsObject()
    }

    private fun parseSearchResult(raw: Any?, source: String): SearchResult {
        val obj = raw as? JSObject ?: return SearchResult(1, 30, source, emptyList(), 0)
        val context = jsContext ?: return SearchResult(1, 30, source, emptyList(), 0)
        val allPage = obj.getIntProperty("allPage") ?: 1
        val limit = obj.getIntProperty("limit") ?: 30
        val total = obj.getIntProperty("total") ?: 0
        val listObj = obj.getJSObject("list")
        val list = mutableListOf<MusicInfo>()
        if (listObj != null && listObj is JSArray) {
            val len = context.length(listObj)
            for (i in 0 until len) {
                val item = context.get(listObj, i) as? JSObject ?: continue
                list.add(jsMusicInfoToModel(item, source))
            }
        }
        return SearchResult(allPage, limit, source, list, total)
    }

    private fun jsMusicInfoToModel(obj: JSObject, fallbackSource: String): MusicInfo {
        val source = obj.getString("source") ?: fallbackSource
        val songmid = obj.getString("songmid") ?: obj.getString("id") ?: ""
        val context = jsContext ?: throw IllegalStateException("SDK not ready")
        val types = mutableListOf<com.lingxu.music.core.model.MusicQuality>()
        val _types = mutableMapOf<String, com.lingxu.music.core.model.QualitySize>()
        val typesObj = obj.getJSObject("types")
        val qualitysObj = obj.getJSObject("_types")
        // types 是数组，_types 是 map
        if (typesObj != null && typesObj is JSArray) {
            val len = context.length(typesObj)
            for (i in 0 until len) {
                val t = context.get(typesObj, i) as? JSObject ?: continue
                val type = t.getString("type") ?: continue
                types.add(
                    com.lingxu.music.core.model.MusicQuality(
                        type = type,
                        size = t.getString("size"),
                        hash = t.getString("hash"),
                    ),
                )
            }
        }
        if (qualitysObj != null) {
            val names = context.getOwnPropertyNames(qualitysObj)
            when (names) {
                is Array<*> -> names.forEach { n ->
                    val name = n.toString()
                    val v = qualitysObj.getJSObject(name) ?: return@forEach
                    _types[name] = com.lingxu.music.core.model.QualitySize(
                        size = v.getString("size"),
                        hash = v.getString("hash"),
                    )
                }
                is JSArray -> {
                    val len = context.length(names)
                    for (i in 0 until len) {
                        val name = (context.get(names, i) as? String) ?: continue
                        val v = qualitysObj.getJSObject(name) ?: continue
                        _types[name] = com.lingxu.music.core.model.QualitySize(
                            size = v.getString("size"),
                            hash = v.getString("hash"),
                        )
                    }
                }
            }
        }
        return MusicInfo(
            id = "${source}_$songmid",
            name = obj.getString("name") ?: "",
            singer = obj.getString("singer") ?: "",
            source = source,
            interval = obj.getString("interval"),
            meta = com.lingxu.music.core.model.MusicMeta(
                songId = songmid,
                albumName = obj.getString("albumName") ?: "",
                picUrl = obj.getString("img")?.ifEmpty { null },
                albumId = obj.getString("albumId"),
            ),
            qualitys = types,
            _qualitys = _types,
            hash = obj.getString("hash"),
            strMediaMid = obj.getString("strMediaMid"),
            copyrightId = obj.getString("copyrightId"),
            lrcUrl = obj.getString("lrcUrl"),
            mrcUrl = obj.getString("mrcUrl"),
            trcUrl = obj.getString("trcUrl"),
        )
    }

    private fun parseBoards(raw: Any?, source: String): List<Board> {
        val obj = raw as? JSObject ?: return emptyList()
        val context = jsContext ?: return emptyList()
        val list = obj.getJSObject("list") as? JSArray ?: return emptyList()
        val boards = mutableListOf<Board>()
        val len = context.length(list)
        for (i in 0 until len) {
            val item = context.get(list, i) as? JSObject ?: continue
            boards.add(
                Board(
                    id = "${source}__${item.getString("id") ?: item.getString("bangid") ?: ""}",
                    name = item.getString("name") ?: "",
                    bangid = item.getString("bangid") ?: item.getString("id") ?: "",
                ),
            )
        }
        return boards
    }

    private fun parseBoardDetail(raw: Any?, source: String, bangId: String): BoardDetail {
        val obj = raw as? JSObject ?: return BoardDetail("${source}__$bangId", "")
        val context = jsContext ?: return BoardDetail("${source}__$bangId", "")
        val list = obj.getJSObject("list") as? JSArray
        val musics = mutableListOf<MusicInfo>()
        if (list != null) {
            val len = context.length(list)
            for (i in 0 until len) {
                val item = context.get(list, i) as? JSObject ?: continue
                musics.add(jsMusicInfoToModel(item, source))
            }
        }
        // 榜单的分页信息（原版 leaderboard.board 用 total/limit 算 maxPage）：
        // 各源默认一次返回整张榜单，limit 缺失时视为单页，避免外部按固定页数盲目翻页。
        val total = obj.getIntProperty("total") ?: musics.size
        val limit = obj.getIntProperty("limit") ?: musics.size.coerceAtLeast(1)
        return BoardDetail(
            id = "${source}__$bangId",
            name = obj.getString("name") ?: "",
            list = musics,
            total = total,
            limit = if (obj.getIntProperty("limit") != null) limit else 0,
            maxPage = if (limit > 0) ((total + limit - 1) / limit).coerceAtLeast(1) else 1,
        )
    }

    private fun parseSongListTags(raw: Any?, source: String): SongListTagInfo {
        val obj = raw as? JSObject ?: return SongListTagInfo(source = source)
        val groups = mutableListOf<SongListTagGroup>()
        (obj.getJSObject("tags") as? JSArray)?.let { arr ->
            val len = jsContext?.let { it.length(arr) } ?: 0
            for (i in 0 until len) {
                val g = jsContext?.get(arr, i) as? JSObject ?: continue
                val tags = mutableListOf<SongListTag>()
                (g.getJSObject("list") as? JSArray)?.let { tagArr ->
                    val tl = jsContext?.length(tagArr) ?: 0
                    for (j in 0 until tl) {
                        val t = jsContext?.get(tagArr, j) as? JSObject ?: continue
                        tags.add(
                            SongListTag(
                                id = t.getString("id") ?: continue,
                                name = t.getString("name") ?: "",
                                parentId = t.getString("parent_id") ?: "",
                                parentName = t.getString("parent_name") ?: "",
                                source = t.getString("source") ?: source,
                            ),
                        )
                    }
                }
                groups.add(SongListTagGroup(name = g.getString("name") ?: "", list = tags))
            }
        }
        val hot = mutableListOf<SongListTag>()
        (obj.getJSObject("hotTag") as? JSArray)?.let { arr ->
            val len = jsContext?.let { it.length(arr) } ?: 0
            for (i in 0 until len) {
                val t = jsContext?.get(arr, i) as? JSObject ?: continue
                hot.add(
                    SongListTag(
                        id = t.getString("id") ?: continue,
                        name = t.getString("name") ?: "",
                        parentId = t.getString("parent_id") ?: "",
                        parentName = t.getString("parent_name") ?: "",
                        source = t.getString("source") ?: source,
                    ),
                )
            }
        }
        return SongListTagInfo(tags = groups, hotTag = hot, source = source)
    }

    private fun parseSongListPage(
        raw: Any?,
        source: String,
        sortId: String,
        tagId: String,
    ): SongListPage {
        val obj = raw as? JSObject ?: return SongListPage(source = source)
        val items = mutableListOf<SongListInfo>()
        (obj.getJSObject("list") as? JSArray)?.let { arr ->
            val len = jsContext?.let { it.length(arr) } ?: 0
            for (i in 0 until len) {
                val item = jsContext?.get(arr, i) as? JSObject ?: continue
                val id = item.getString("id") ?: continue
                items.add(
                    SongListInfo(
                        id = id,
                        name = item.getString("name") ?: "",
                        author = item.getString("author") ?: "",
                        img = item.getString("img"),
                        playCount = item.getString("play_count"),
                        total = item.getString("total"),
                        desc = item.getString("desc"),
                        source = item.getString("source") ?: source,
                    ),
                )
            }
        }
        val total = obj.getIntProperty("total") ?: items.size
        val limit = obj.getIntProperty("limit") ?: 30
        return SongListPage(
            list = items,
            total = total,
            page = obj.getIntProperty("page") ?: 1,
            limit = limit,
            maxPage = if (limit > 0) ((total + limit - 1) / limit).coerceAtLeast(1) else 1,
            source = source,
            tagId = tagId,
            sortId = sortId,
        )
    }

    private fun parseSongListDetail(raw: Any?, source: String, id: String): SongListDetail {
        val obj = raw as? JSObject ?: return SongListDetail(source = source)
        val musics = mutableListOf<MusicInfo>()
        (obj.getJSObject("list") as? JSArray)?.let { arr ->
            val len = jsContext?.let { it.length(arr) } ?: 0
            for (i in 0 until len) {
                val item = jsContext?.get(arr, i) as? JSObject ?: continue
                musics.add(jsMusicInfoToModel(item, source))
            }
        }
        val total = obj.getIntProperty("total") ?: musics.size
        val limit = obj.getIntProperty("limit") ?: 30
        val info = obj.getJSObject("info")
        return SongListDetail(
            list = musics,
            source = source,
            total = total,
            page = obj.getIntProperty("page") ?: 1,
            limit = limit,
            maxPage = if (limit > 0) ((total + limit - 1) / limit).coerceAtLeast(1) else 1,
            id = id,
            name = info?.getString("name") ?: obj.getString("name") ?: "",
            img = info?.getString("img") ?: obj.getString("img"),
            desc = info?.getString("desc") ?: obj.getString("desc"),
            author = info?.getString("author") ?: "",
            playCount = info?.getString("play_count") ?: obj.getString("play_count"),
        )
    }

    private fun parseHotSearch(raw: Any?, source: String): List<String> {
        val obj = raw as? JSObject ?: return emptyList()
        val context = jsContext ?: return emptyList()
        val list = obj.getJSObject("list") as? JSArray ?: return emptyList()
        val words = mutableListOf<String>()
        val len = context.length(list)
        for (i in 0 until len) {
            val item = context.get(list, i) as? JSObject ?: continue
            item.getString("text")?.let { words.add(it) }
            item.getString("name")?.let { words.add(it) }
        }
        return words.distinct()
    }

    private fun parseLyric(raw: Any?): LyricInfo {
        val obj = raw as? JSObject ?: throw IllegalStateException("invalid lyric response")
        val lyric = obj.getString("lyric") ?: throw IllegalStateException("invalid lyric")
        return LyricInfo(
            lyric = lyric,
            tlyric = obj.getString("tlyric"),
            rlyric = obj.getString("rlyric"),
            lxlyric = obj.getString("lxlyric"),
        )
    }

    /**
     * 在独立线程执行原生 HTTP，完成后回到 SDK 线程调 JS 侧的 __lxFetchDone（按 id 查找回调，
     * 不跨线程持 JS 函数引用）。
     */
    private fun startFetch(id: Int, url: String, optionsJson: String) {
        Thread {
            try {
                val opt = json.parseToJsonElement(optionsJson) as? JsonObject ?: JsonObject(emptyMap())
                val method = (opt["method"] as? JsonPrimitive)?.content ?: "get"
                val headers = (opt["headers"] as? JsonObject)?.entries?.mapNotNull { (k, v) ->
                    (v as? JsonPrimitive)?.content?.let { k to it }
                }?.toMap() ?: emptyMap()
                val body = (opt["body"] as? JsonPrimitive)?.content
                val resp = HttpClient.instance.fetchSync(url, method, headers, body, binary = true)
                val bytes = resp.body as? ByteArray ?: ByteArray(0)
                val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val headersJson = buildJsonObject {
                    resp.headers.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
                }.toString()
                val status = resp.statusCode
                val statusText = resp.statusMessage
                executor.execute { callFetchDone(id, true, status, statusText, b64, headersJson) }
            } catch (e: Exception) {
                Log.e(TAG, "fetch failed: ${e.message}")
                val msg = e.message ?: "fetch failed"
                executor.execute { callFetchDone(id, false, msg, "", "", "{}") }
            }
        }.apply { isDaemon = true }.start()
    }

    /** 回到 SDK 线程调用 JS 侧 __lxFetchDone（在 SDK 线程执行）；payload 为响应体的 base64 */
    private fun callFetchDone(
        id: Int,
        ok: Boolean,
        status: Any?,
        statusText: Any?,
        payload: Any?,
        headersJson: Any?,
    ) {
        val fn = jsContext?.getGlobalObject()?.getJSFunction("__lxFetchDone") ?: return
        fn.call(id, ok, status, statusText, payload, headersJson)
    }

    /** 安排一次性/重复定时器；到点后回到 SDK 线程调用 JS 侧 __lxTimerFire */
    private fun scheduleTimer(args: Array<Any?>, repeat: Boolean): Any? {
        val id = (args.getOrNull(0) as? Number)?.toInt() ?: return null
        val delay = ((args.getOrNull(1) as? Number)?.toLong() ?: 0L).coerceAtLeast(0L)
        val task = Runnable { executor.execute { callTimerFire(id, repeat) } }
        val future = if (repeat) {
            timerExecutor.scheduleAtFixedRate(task, delay, delay.coerceAtLeast(1L), TimeUnit.MILLISECONDS)
        } else {
            timerExecutor.schedule(task, delay, TimeUnit.MILLISECONDS)
        }
        timers[id] = future
        return null
    }

    private fun cancelTimer(args: Array<Any?>) {
        val id = (args.getOrNull(0) as? Number)?.toInt() ?: return
        timers.remove(id)?.cancel(false)
    }

    private fun callTimerFire(id: Int, repeat: Boolean) {
        // 一次性定时器执行后要把条目摘掉，否则 map 会随每次 setTimeout 增长
        if (!repeat) timers.remove(id)
        val fn = jsContext?.getGlobalObject()?.getJSFunction("__lxTimerFire") ?: return
        fn.call(id, repeat)
    }
}
