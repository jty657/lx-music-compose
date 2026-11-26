package com.lingxu.music.core.system

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 应用内运行日志环形缓冲（对齐原版 `utils/log.ts` 的 getLogs / clearLogs）。
 *
 * 原版把音源脚本 console、错误日志写入文件，设置页可查看/清空；移植版此前日志只打到
 * logcat，普通用户无法在应用内查看，音源/搜索出问题时难以自查。本对象提供一个纯内存、
 * 有上限的缓冲，把已经打进 logcat 的同一份文本顺带留一份在这里，供设置页展示。
 *
 * 设计约束：
 * - 零依赖、零落盘：仅 JDK/Android 基础类，不新增偏好键、不写文件（不扩大备份/隐私面）。
 * - 有界：最多 [MAX_LINES] 行，超出丢弃最旧，长期运行内存恒定。
 * - 线程安全：console 回调用 [ConcurrentLinkedDeque]，SDK 线程与主线程可并发写入。
 * - 旁路、可失败：所有写入方用 runCatching 包裹，日志设施的任何异常都不影响音源/播放。
 */
object AppLog {
    /** 保留的行数上限：覆盖最近若干次搜索/换源，足够排查又不让内存无限增长 */
    private const val MAX_LINES = 300

    /** 每行最多字符数：脚本 console 可能打印整段 JSON，截断避免单行撑破弹窗 */
    private const val MAX_LINE_CHARS = 500

    private val lines = ConcurrentLinkedDeque<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    /** 追加一条日志。level 与原版一致：log / info / warn / error。 */
    @Synchronized
    fun append(level: String, message: String) {
        val oneLine = message.replace('\r', ' ').replace('\n', ' ').trim()
        if (oneLine.isEmpty()) return
        val clipped = if (oneLine.length > MAX_LINE_CHARS) oneLine.take(MAX_LINE_CHARS) + "…" else oneLine
        val stamp = try {
            timeFormat.format(Date())
        } catch (_: Exception) {
            ""
        }
        lines.addLast("$stamp [$level] $clipped")
        while (lines.size > MAX_LINES) lines.pollFirst()
    }

    /** 快照（最旧在前），供设置页一次性取全文展示；空则返回空串。 */
    fun snapshot(): String = lines.joinToString("\n")

    /** 当前行数，用于设置项副标题与空态判断 */
    fun size(): Int = lines.size

    /** 清空（对齐原版 clearLogs） */
    fun clear() = lines.clear()
}
