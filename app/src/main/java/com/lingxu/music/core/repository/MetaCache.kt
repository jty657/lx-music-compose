package com.lingxu.music.core.repository

import android.util.LruCache
import com.lingxu.music.core.model.LyricInfo

/**
 * 歌词与播放链接的内存缓存（对齐原版 `utils/data.ts` 里的 lyric / musicUrl 缓存）。
 *
 * ### 为什么要有它
 * 原版把歌词与取的播放地址缓存在本地存储里，切歌、返回上一首、换首再回来都不会重复请求；
 * 移植版此前**每次切歌都重新取词**，在线播放地址也每次重解析——弱网下切回上一首要等 1~2 秒，
 * 歌词区先空一屏才出现。这里的收益比原版更明显：移植版的歌词还要走「当前源无效 → 五源换源」，
 * 没有缓存时单次切歌最坏能触发 6 次网络请求。
 *
 * ### 为什么是内存 LRU 而不是落盘
 * 落盘要新增序列化文件、并发写与损坏隔离逻辑（这几项本项目已踩过坑），属于为性能引入新风险；
 * 而移植版进程常驻（前台服务持有播放器），内存 LRU 已覆盖「切歌/换首/回上一首」这些高频场景。
 * 冷启动后重新取词一次，与原版首次使用一致。**这也是与原版的一处已知差异，已记录在报告里。**
 *
 * ### 缓存键为什么带 source
 * 同一首歌在不同音源下 id 可能相同、歌词与链接却不同（尤其换源取词之后），
 * 只用 id 做键会把 A 源的歌词喂给 B 源。
 */
object MetaCache {

    /** 歌词缓存：按「源 + 歌曲 id」键，最多 300 条（约几十 KB 文本，LRU 自动淘汰） */
    private val lyricCache = object : LruCache<String, LyricInfo>(300) {}

    /**
     * 播放地址缓存：链接有签名过期时间（多数音源约 10~30 分钟），
     * 因此额外记录写入时刻，超过 [URL_TTL_MS] 视为失效，避免把过期链接喂给播放器。
     */
    private class UrlEntry(val url: String, val at: Long)

    private val urlCache = object : LruCache<String, UrlEntry>(200) {}

    /** 播放地址有效期：保守取 10 分钟（原版链接普遍 20~30 分钟有效） */
    private const val URL_TTL_MS = 10 * 60 * 1000L

    private fun lyricKey(source: String, id: String) = "$source|$id"

    private fun urlKey(source: String, id: String, quality: String) = "$source|$id|$quality"

    // ================= 歌词 =================

    fun getLyric(source: String, id: String): LyricInfo? = lyricCache.get(lyricKey(source, id))

    fun putLyric(source: String, id: String, info: LyricInfo) {
        lyricCache.put(lyricKey(source, id), info)
    }

    // ================= 播放地址 =================

    fun getUrl(source: String, id: String, quality: String): String? {
        val entry = urlCache.get(urlKey(source, id, quality)) ?: return null
        if (System.currentTimeMillis() - entry.at > URL_TTL_MS) {
            urlCache.remove(urlKey(source, id, quality))
            return null
        }
        return entry.url
    }

    fun putUrl(source: String, id: String, quality: String, url: String) {
        urlCache.put(urlKey(source, id, quality), UrlEntry(url, System.currentTimeMillis()))
    }

    /** 单曲缓存失效（菜单「清除链接缓存」用）：同时清掉各音质档位的地址与该曲歌词 */
    fun invalidate(source: String, id: String) {
        urlCache.snapshot().keys
            .filter { it.startsWith("$source|$id|") }
            .forEach { urlCache.remove(it) }
        lyricCache.remove(lyricKey(source, id))
    }

    /**
     * 只作废该曲的播放地址（播放报错时调用）。
     * 报错通常意味着链接被判失效（签名过期/防盗链），不清掉的话
     * 「出错 5 秒后自动下一首 → 用户切回来」会反复拿到同一个坏链接。
     */
    fun invalidateUrl(source: String, id: String) {
        urlCache.snapshot().keys
            .filter { it.startsWith("$source|$id|") }
            .forEach { urlCache.remove(it) }
    }

    // ================= 统计与清理（对齐原版 MetaCache 设置项） =================

    /** 已缓存的歌词条数 */
    fun lyricCount(): Int = lyricCache.snapshot().size

    /** 已缓存的播放地址条数（含过期未淘汰项，便于用户理解「清完变小」） */
    fun urlCount(): Int = urlCache.snapshot().size

    /** 清空全部歌词缓存，返回清理条数 */
    fun clearLyric(): Int {
        val n = lyricCount()
        lyricCache.evictAll()
        return n
    }

    /** 清空全部播放地址缓存，返回清理条数 */
    fun clearUrl(): Int {
        val n = urlCount()
        urlCache.evictAll()
        return n
    }
}
