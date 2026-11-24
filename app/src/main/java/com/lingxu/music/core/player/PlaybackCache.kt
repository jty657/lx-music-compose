package com.lingxu.music.core.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * 播放缓存（对齐原版 `player.cacheSize`）。
 *
 * ### 为什么要有它
 * 原版 react-native-track-player 用 `maxCacheSize` 开启离线缓存：听过的歌再次播放直接读本地
 * 缓存，既省流量，也避免「歌源抽风但缓存还能放」。移植版此前**完全没有播放缓存**——
 * 同一首歌反复播放每次都要回源，弱网/切后台回来经常卡在 buffering。
 *
 * ### 为什么不用新增依赖
 * `media3-datasource` 是 `media3-exoplayer` 的传递依赖，`SimpleCache` / `CacheDataSource`
 * 都在其中，直接可用；不新增坐标、不改锁文件。
 *
 * ### 与播放内核的边界
 * 只替换 ExoPlayer 的数据源链（`cache -> http`），不改队列、不改状态机、不改播放逻辑。
 * 缓存读写失败时 [CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR] 会直接回源，最坏退化为原行为。
 *
 * ### 为什么用「可切换工厂」
 * media3 1.8.0 的 `ExoPlayer` 实例**没有** `setMediaSourceFactory`（只有 Builder 有），
 * 而重建播放器会打断正在播放的歌曲。这里让 ExoPlayer 永久持有一个稳定的工厂实例，
 * 内部按当前缓存实例决定套不套缓存层：切换容量只换引用，播放器无感。
 */
@UnstableApi
object PlaybackCache {

    /** 原版 `player.cacheSize` 单位是 KB，默认 1024 */
    const val DEFAULT_SIZE_KB: Int = 1024

    /** 上限：原版允许 1GB；这里收敛到 512MB，移动端音频缓存足够且卸载残留可控 */
    const val MAX_SIZE_KB: Int = 512 * 1024

    /** 0 表示不缓存（对齐原版：cacheSize 为 0 时等同关闭） */
    const val DISABLED_SIZE_KB: Int = 0

    /** 缓存目录：`<cacheDir>/player`，与封面 HTTP 缓存（`<cacheDir>/http`）分开，便于单独清理 */
    fun cacheDir(context: Context): File = File(context.applicationContext.cacheDir, "player")

    /** 当前容量（KB） */
    @Volatile
    var sizeKb: Int = DEFAULT_SIZE_KB
        private set

    /** 当前缓存实例；null 表示不缓存 */
    @Volatile
    var cache: SimpleCache? = null
        private set

    /** ExoPlayer 持有的稳定工厂（只创建一次） */
    private var factory: SwitchableDataSourceFactory? = null

    /** 把当前缓存实例同步给工厂（缓存重建后必须调用，否则工厂仍指向已释放的旧实例） */
    private fun syncFactory() {
        factory?.cache = cache
    }

    val enabled: Boolean
        get() = cache != null

    /**
     * 取（必要时创建）指定容量的缓存实例。容量变化时释放旧实例并按新容量重建。
     * 与原版「改 `cacheSize` 后重开缓存」语义一致，但只重建缓存层，不重建播放器。
     *
     * @return 缓存实例；容量为 0 或目录不可用时返回 null
     */
    @Synchronized
    fun get(context: Context, sizeKb: Int): SimpleCache? {
        val target = sizeKb.coerceIn(DISABLED_SIZE_KB, MAX_SIZE_KB)
        if (target == DISABLED_SIZE_KB) {
            release()
            this.sizeKb = target
            return null
        }
        cache?.let { if (this.sizeKb == target) return it }
        release()
        cache = runCatching {
            SimpleCache(
                cacheDir(context),
                LeastRecentlyUsedCacheEvictor(target.toLong() * 1024L),
                StandaloneDatabaseProvider(context.applicationContext),
            )
        }.getOrNull()
        this.sizeKb = if (cache != null) target else DISABLED_SIZE_KB
        syncFactory()
        return cache
    }

    /** 当前缓存占用（字节） */
    fun sizeBytes(): Long = runCatching { cache?.cacheSpace ?: 0L }.getOrDefault(0L)

    /**
     * 清空缓存条目。逐个删除 key 而不是删目录：删目录会让 ExoPlayer 已持有的
     * CacheDataSource 指向不存在的目录；保留实例只清内容，后续播放仍可写入。
     *
     * @return 清理前的占用字节数
     */
    @Synchronized
    fun clear(): Long {
        val c = cache ?: return 0L
        val before = runCatching { c.cacheSpace }.getOrDefault(0L)
        runCatching { c.keys.toList().forEach { key -> c.removeResource(key) } }
        return before
    }

    @Synchronized
    fun release() {
        runCatching { cache?.release() }
        cache = null
        syncFactory()
    }

    /**
     * 供 ExoPlayer 使用的**稳定**数据源工厂：命中缓存读缓存，未命中走网络并写入缓存。
     * 工厂实例只创建一次，内部按 [cache] 动态决定是否套缓存层。
     */
    @Synchronized
    fun dataSourceFactory(context: Context): DataSource.Factory {
        factory?.let { it.cache = cache; return it }
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent(
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36",
            )
            // 部分音源会 302 到 http，跨协议重定向不放行会直接失败
            .setAllowCrossProtocolRedirects(true)
        // DefaultDataSource 负责按 scheme 分派（file / content / http），http 交给上面的工厂
        val base = DefaultDataSource.Factory(context.applicationContext, http)
        return SwitchableDataSourceFactory(base, cache).also { factory = it }
    }

    /** 可切换的数据源工厂：每次 create 时按当前缓存实例组装 `cache -> base` 或直接用 base */
    private class SwitchableDataSourceFactory(
        private val base: DataSource.Factory,
        @Volatile var cache: SimpleCache?,
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            val c = cache ?: return base.createDataSource()
            return CacheDataSource.Factory()
                .setCache(c)
                .setUpstreamDataSourceFactory(base)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .createDataSource()
        }
    }
}
