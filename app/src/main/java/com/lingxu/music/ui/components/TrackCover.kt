package com.lingxu.music.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingxu.music.core.request.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.concurrent.ConcurrentHashMap

/** 封面内存缓存上限（按解码后像素字节计） */
private const val COVER_CACHE_BYTES = 8 * 1024 * 1024

/** 解码目标边长夹取范围（像素） */
private const val COVER_MIN_PX = 96
private const val COVER_MAX_PX = 1024

/** 加载失败后的冷却时间；到点后允许重试（失败结果不能永久缓存，否则一次网络抖动就永远是占位色块） */
private const val COVER_FAIL_TTL_MS = 60_000L

/** 同时进行的封面请求上限，避免快速滑动时把 IO 线程池压满 */
private const val COVER_MAX_CONCURRENCY = 4

/** 按字节计量的 LRU；超出上限自动淘汰最久未用的位图 */
private class CoverLruCache : LruCache<String, Bitmap>(COVER_CACHE_BYTES) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

private val coverCache = CoverLruCache()
private val coverFailedAt = object : LruCache<String, Long>(256) {}
private val coverInFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()
private val coverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private val coverSemaphore = Semaphore(COVER_MAX_CONCURRENCY)

/** 同一 URL+尺寸的并发请求只发一次 */
private fun requestCover(cacheKey: String, url: String, targetPx: Int): Deferred<Bitmap?> =
    coverInFlight.getOrPut(cacheKey) {
        coverScope.async {
            try {
                coverSemaphore.withPermit {
                    val bytes = HttpClient.instance.fetchBytesSync(url)
                    if (bytes == null) null else decodeDownsampled(bytes, targetPx)
                }
            } catch (e: Exception) {
                null
            } finally {
                coverInFlight.remove(cacheKey)
            }
        }
    }

private fun decodeDownsampled(bytes: ByteArray, maxPx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

/**
 * 加载封面位图。统一走 [HttpClient.fetchBytesSync]：
 * fetchSync 的 body 是 UTF-8 字符串，直接 decodeByteArray 会永远失败。
 * 缓存 key 含目标尺寸：列表行按小图解码，播放页按大图，避免「列表占满缓存、播放页发虚」。
 */
@Composable
private fun rememberCoverBitmap(url: String?, targetPx: Int): Bitmap? {
    val cacheKey = url?.let { "$it@$targetPx" }
    var bitmap by remember(cacheKey) {
        mutableStateOf(cacheKey?.let { coverCache.get(it) })
    }
    LaunchedEffect(cacheKey, url) {
        if (url.isNullOrBlank() || cacheKey == null) {
            bitmap = null
            return@LaunchedEffect
        }
        val cached = coverCache.get(cacheKey)
        if (cached != null) {
            bitmap = cached
            return@LaunchedEffect
        }
        val failedAt = coverFailedAt.get(cacheKey)
        if (failedAt != null && System.currentTimeMillis() - failedAt < COVER_FAIL_TTL_MS) {
            bitmap = null
            return@LaunchedEffect
        }
        val loaded = runCatching { requestCover(cacheKey, url, targetPx).await() }.getOrNull()
        if (loaded != null) {
            coverCache.put(cacheKey, loaded)
            coverFailedAt.remove(cacheKey)
        } else {
            coverFailedAt.put(cacheKey, System.currentTimeMillis())
        }
        bitmap = loaded
    }
    return bitmap
}

/** 圆角封面图；url 为空或加载失败时显示占位底色 */
@Composable
fun TrackCover(
    url: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = 8.dp,
) {
    val density = LocalDensity.current
    // 2x 超采样避免放大发虚；列表行 44dp 与播放页 288dp 会得到不同的解码尺寸
    val targetPx = remember(size, density) {
        (with(density) { size.toPx() } * 2f).toInt().coerceIn(COVER_MIN_PX, COVER_MAX_PX)
    }
    val bitmap = rememberCoverBitmap(url, targetPx)
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f)),
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
