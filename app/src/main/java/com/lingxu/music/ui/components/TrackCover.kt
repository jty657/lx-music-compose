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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingxu.music.core.request.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.concurrent.ConcurrentHashMap

/** 封面内存缓存上限（按解码后像素字节计） */
private const val COVER_CACHE_BYTES = 8 * 1024 * 1024

/** 解码目标最长边：列表/播放器都远小于原图，降采样能显著降低内存与解码耗时 */
private const val COVER_MAX_PX = 512

/** 加载失败后的冷却时间；到点后允许重试（原先失败结果永久缓存，一次网络抖动就永远是占位色块） */
private const val COVER_FAIL_TTL_MS = 60_000L

/** 按字节计量的 LRU；超出上限自动淘汰最久未用的位图 */
private class CoverLruCache : LruCache<String, Bitmap>(COVER_CACHE_BYTES) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

private val coverCache = CoverLruCache()
private val coverFailedAt = ConcurrentHashMap<String, Long>()
private val coverInFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()
private val coverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** 同一 URL 的并发请求只发一次 */
private fun requestCover(url: String): Deferred<Bitmap?> = coverInFlight.getOrPut(url) {
    coverScope.async {
        try {
            val bytes = HttpClient.instance.fetchBytesSync(url)
            if (bytes == null) null else decodeDownsampled(bytes, COVER_MAX_PX)
        } catch (e: Exception) {
            null
        } finally {
            coverInFlight.remove(url)
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
 */
@Composable
private fun rememberCoverBitmap(url: String?): Bitmap? {
    var bitmap by remember(url) { mutableStateOf(url?.let { coverCache.get(it) }) }
    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            bitmap = null
            return@LaunchedEffect
        }
        val cached = coverCache.get(url)
        if (cached != null) {
            bitmap = cached
            return@LaunchedEffect
        }
        val failedAt = coverFailedAt[url]
        if (failedAt != null && System.currentTimeMillis() - failedAt < COVER_FAIL_TTL_MS) {
            bitmap = null
            return@LaunchedEffect
        }
        val loaded = runCatching { requestCover(url).await() }.getOrNull()
        if (loaded != null) {
            coverCache.put(url, loaded)
            coverFailedAt.remove(url)
        } else {
            coverFailedAt[url] = System.currentTimeMillis()
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
    val bitmap = rememberCoverBitmap(url)
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f)),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
