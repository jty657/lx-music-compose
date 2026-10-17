package com.lingxu.music.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 封面位图缓存：按 URL 缓存（包含失败结果，避免重复请求） */
private val coverCache = java.util.concurrent.ConcurrentHashMap<String, Bitmap?>()

/**
 * 加载封面位图。统一走 [HttpClient.fetchBytesSync]：
 * fetchSync 的 body 是 UTF-8 字符串，直接 decodeByteArray 会永远失败。
 */
@Composable
private fun rememberCoverBitmap(url: String?): Bitmap? {
    var bitmap by remember(url) { mutableStateOf(url?.let { coverCache[it] }) }
    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            bitmap = null
            return@LaunchedEffect
        }
        if (coverCache.containsKey(url)) {
            bitmap = coverCache[url]
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                HttpClient.instance.fetchBytesSync(url)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            }.getOrNull()
        }
        coverCache[url] = loaded
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
