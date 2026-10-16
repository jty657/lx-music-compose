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
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.util.concurrent.Executors
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

        @Volatile
        private var loaderInited = false
    }

    private val sdkBundle: String = context.assets.open("engine/music-sdk.bundle.js").use {
        it.readBytes().toString(Charsets.UTF_8)
    }

    /**
     * Buffer polyfill。bundle 的 esbuild 配置是 platform:neutral，不会注入全局 Buffer，
     * 而 QuickJS 也没有 Node 的 Buffer，不先注入会报 "'Buffer' is not defined" 导致 SDK 初始化失败。
     */
    private val bufferPolyfill: String = context.assets.open("engine/buffer-polyfill.js").use {
        it.readBytes().toString(Charsets.UTF_8)
    }

    /** 单线程执行器：QuickJSContext 同线程要求 */
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "BuiltinSDK").apply { isDaemon = true }
    }

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

                // 注入全局 fetch（SDK request.js 用 global.fetch）
                global.setProperty("fetch") { args: Array<Any?> ->
                    val url = args[0] as String
                    val options = args.getOrNull(1) as? JSObject
                    createDeferredFetch(context, url, options)
                }
                // AbortController 桩（fetch 的 signal 参数需要）
                global.setProperty("AbortController") { _: Array<Any?> ->
                    val ctrl = context.createNewJSObject()
                    ctrl.setProperty("signal", context.createNewJSObject())
                    ctrl.setProperty("abort") { _: Array<Any?> -> null }
                    ctrl
                }

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
            // 并发搜索全部内置源
            val sources = listOf("kw", "kg", "tx", "wy", "mg")
            val results = sources.map { s ->
                runCatching { search(text, page, s, limit) }
                    .getOrElse {
                        SearchResult(1, limit, s, emptyList(), 0)
                    }
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
        return withSdkThread {
            val module = getModule(source) ?: throw IllegalStateException("source not found: $source")
            val musicSearch = module.getJSObject("musicSearch") ?: throw IllegalStateException("musicSearch not found")
            val fn = musicSearch.getJSFunction("search")
            val raw = callJSFunction(fn, arrayOf(text, page, limit))
            parseSearchResult(raw, source)
        }
    }

    // ================= 榜单 =================

    suspend fun getBoardsList(source: String): List<Board> = withSdkThread {
        val module = getModule(source) ?: return@withSdkThread emptyList()
        val leaderboard = module.getJSObject("leaderboard") ?: return@withSdkThread emptyList()
        val fn = leaderboard.getJSFunction("getBoards") ?: return@withSdkThread emptyList()
        val raw = callJSFunction(fn, arrayOf())
        parseBoards(raw, source)
    }

    suspend fun getBoardList(source: String, bangId: String, page: Int): BoardDetail = withSdkThread {
        val module = getModule(source) ?: return@withSdkThread BoardDetail("${source}__$bangId", "")
        val leaderboard = module.getJSObject("leaderboard") ?: return@withSdkThread BoardDetail("${source}__$bangId", "")
        val fn = leaderboard.getJSFunction("getList") ?: return@withSdkThread BoardDetail("${source}__$bangId", "")
        val raw = callJSFunction(fn, arrayOf(bangId, page))
        parseBoardDetail(raw, source, bangId)
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
        val raw = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getTags") ?: return@withSdkThread null
            callJSFunction(fn, arrayOf())
        } ?: return SongListTagInfo(source = source)
        return parseSongListTags(awaitPromise(asPromise(raw)), source)
    }

    /** 歌单列表 */
    suspend fun getSongList(source: String, sortId: String, tagId: String, page: Int): SongListPage {
        val raw = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getList") ?: return@withSdkThread null
            callJSFunction(fn, arrayOf(sortId, tagId, page))
        } ?: return SongListPage(source = source, sortId = sortId, tagId = tagId)
        return parseSongListPage(awaitPromise(asPromise(raw)), source, sortId, tagId)
    }

    /** 歌单详情（歌单内歌曲） */
    suspend fun getSongListDetail(source: String, id: String, page: Int): SongListDetail {
        val raw = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("getListDetail") ?: return@withSdkThread null
            callJSFunction(fn, arrayOf(id, page))
        } ?: return SongListDetail(source = source, id = id)
        return parseSongListDetail(awaitPromise(asPromise(raw)), source, id)
    }

    /** 搜索歌单（对齐原版 core/search/songlist.ts） */
    suspend fun searchSongList(text: String, page: Int, source: String, limit: Int = 18): SongListPage {
        val raw = withSdkThread {
            val songList = getSongListModule(source) ?: return@withSdkThread null
            val fn = songList.getJSFunction("search") ?: return@withSdkThread null
            callJSFunction(fn, arrayOf(text, page, limit))
        } ?: return SongListPage(source = source)
        return parseSongListPage(awaitPromise(asPromise(raw)), source, "", "")
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

    suspend fun getHotSearch(source: String): List<String> = withSdkThread {
        val module = getModule(source) ?: return@withSdkThread emptyList()
        val hotSearch = module.getJSObject("hotSearch") ?: return@withSdkThread emptyList()
        val fn = hotSearch.getJSFunction("getList") ?: return@withSdkThread emptyList()
        val raw = callJSFunction(fn, arrayOf())
        parseHotSearch(raw, source)
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
        return BoardDetail(
            id = "${source}__$bangId",
            name = obj.getString("name") ?: "",
            list = musics,
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
     * 构造一个已 resolve 的 thenable：SDK 的 fetchData 会对 text()/blob()/json() 的返回值调 .then()
     * QuickJS wrapper 无 Promise 构造函数桥，直接返回带 then/catch 的对象即可被 await
     */
    private fun resolvedPromise(context: QuickJSContext, value: Any?): JSObject {
        val obj = context.createNewJSObject()
        obj.setProperty("then", JSCallFunction { args ->
            (args.getOrNull(0) as? JSCallFunction)?.call(value)
            obj
        })
        obj.setProperty("catch", JSCallFunction { obj })
        return obj
    }

    /** 创建 fetch Promise 桥接（SDK 内 httpFetch 用） */
    private fun createDeferredFetch(context: QuickJSContext, url: String, options: JSObject?): JSObject {
        // 手动构造 Promise 对象（QuickJS wrapper 无内置 Promise 构造函数桥）
        // 方案：直接返回 thenable 对象 —— JS 侧 await 会等 then 回调
        val promiseObj = context.createNewJSObject()
        val thenCallbacks = mutableListOf<Pair<JSCallFunction?, JSCallFunction?>>()
        promiseObj.setProperty("then") { args: Array<Any?> ->
            val onFulfilled = args.getOrNull(0) as? JSCallFunction
            val onRejected = args.getOrNull(1) as? JSCallFunction
            thenCallbacks.add(onFulfilled to onRejected)
            promiseObj
        }
        promiseObj.setProperty("catch") { args: Array<Any?> ->
            promiseObj
        }

        // 在 IO 线程执行 fetch，完成后回调
        Thread {
            try {
                val method = options?.getString("method") ?: "get"
                val headersObj = options?.getJSObject("headers")
                val headers = mutableMapOf<String, String>()
                if (headersObj != null) {
                    val names = context.getOwnPropertyNames(headersObj) as? Array<*>
                    names?.forEach { n ->
                        headersObj.getString(n.toString())?.let { headers[n.toString()] = it }
                    }
                }
                val bodyStr = options?.getString("body")
                val resp = HttpClient.instance.fetchSync(url, method, headers, bodyStr)
                // 构造 Response 对象；text()/blob()/json() 必须返回可 then 的 Promise
                val respObj = context.createNewJSObject()
                respObj.setProperty("status", resp.statusCode)
                respObj.setProperty("statusCode", resp.statusCode)
                respObj.setProperty("statusText", resp.statusMessage)
                respObj.setProperty("ok", resp.statusCode in 200..299)
                // SDK 的 request.js 读 resp.headers.map
                val mapObj = context.createNewJSObject()
                resp.headers.forEach { (k, v) -> mapObj.setProperty(k, v) }
                val headersOut = context.createNewJSObject()
                headersOut.setProperty("map", mapObj)
                respObj.setProperty("headers", headersOut)
                respObj.setProperty("url", url)

                val textFn = JSCallFunction {
                    resolvedPromise(context, resp.body as? String ?: "")
                }
                respObj.setProperty("text", textFn)
                respObj.setProperty("blob", textFn)
                respObj.setProperty("json", JSCallFunction {
                    val body = resp.body as? String ?: "null"
                    val parsed = runCatching { context.evaluate("($body)") }.getOrNull()
                        ?: runCatching { context.evaluate("JSON.parse(${Json.encodeToString(body)})") }.getOrNull()
                    resolvedPromise(context, parsed)
                })
                // 触发 then 回调（在主 SDK 线程执行）
                executor.execute {
                    thenCallbacks.forEach { (onFulfilled, _) ->
                        onFulfilled?.call(respObj)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetch failed: ${e.message}")
                executor.execute {
                    thenCallbacks.forEach { (_, onRejected) ->
                        onRejected?.call(e.message ?: "fetch failed")
                    }
                }
            }
        }.start()

        return promiseObj
    }
}
