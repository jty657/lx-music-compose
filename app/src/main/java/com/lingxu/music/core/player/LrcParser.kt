package com.lingxu.music.core.player

import com.lingxu.music.core.model.LyricInfo

/** LRC 歌词解析（对应原版 utils/lrcTools.ts 的基础解析） */
object LrcParser {

    /**
     * 时间标签：兼容 2 段（分:秒）与 3 段（时:分:秒）。
     * 原版 timeExp 为 `\d{1,3}(:\d{1,3}){0,2}(?:\.\d{1,3})`，移植版此前只认 2 段，
     * 带小时的长歌词整行会被跳过。分组：1=首段, 2=次段, 3=三段(可选), 4=小数。
     */
    private val TIME_TAG = Regex("\\[(\\d{1,3}):(\\d{1,2})(?::(\\d{1,2}))?(?:[.:](\\d{1,3}))?]")

    /** 整体时间偏移标签 [offset:±ms]（正数表示歌词整体提前） */
    private val OFFSET_TAG = Regex("\\[offset:\\s*([+-]?\\d+)\\s*]", RegexOption.IGNORE_CASE)

    /** 解析歌词信息：主歌词 + 翻译/罗马音（同时间点合并到 translation/roma） */
    fun parse(info: LyricInfo?): List<LyricLine> {
        if (info == null) return emptyList()
        // 主歌词实际生效的 offset。翻译/罗马音通常与主歌词共用同一条时间轴，自身不带
        // [offset:] 标签；若各自按 0 解析，时间会整体错位，translations[line.timeMs]
        // 全部落空，表现为「翻译/罗马音静默不显示」。故这里提取主歌词 offset 并传给它们。
        val mainOffset = OFFSET_TAG.find(info.lyric ?: "")?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val main = parseSingle(info.lyric, mainOffset)
        if (main.isEmpty()) return emptyList()
        val translations = if (info.tlyric.isNullOrBlank()) {
            emptyMap()
        } else {
            parseSingle(info.tlyric, mainOffset).associate { it.timeMs to it.text }
        }
        val romas = if (info.rlyric.isNullOrBlank()) {
            emptyMap()
        } else {
            parseSingle(info.rlyric, mainOffset).associate { it.timeMs to it.text }
        }
        return main.map { line ->
            line.copy(
                translation = translations[line.timeMs]?.takeIf { it != line.text },
                roma = romas[line.timeMs]?.takeIf { it != line.text },
            )
        }
    }

    /**
     * 解析单段 LRC 文本；多时间标签行会展开成多行，返回按时间升序的列表。
     * @param offsetOverride 当本段文本没有自己的 [offset:] 标签时采用的 offset。
     *   翻译/罗马音传入主歌词的 offset，保证三者时间轴对齐。
     */
    fun parseSingle(lrc: String?, offsetOverride: Long? = null): List<LyricLine> {
        if (lrc.isNullOrBlank()) return emptyList()
        val offset = OFFSET_TAG.find(lrc)?.groupValues?.get(1)?.toLongOrNull() ?: offsetOverride ?: 0L
        val lines = mutableListOf<LyricLine>()
        for (raw in lrc.split('\n', '\r')) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            val matches = TIME_TAG.findAll(line).toList()
            if (matches.isEmpty()) continue
            val text = line.substring(matches.last().range.last + 1).trim()
            if (text.isEmpty()) continue
            for (m in matches) {
                val first = m.groupValues[1].toLongOrNull() ?: continue
                val second = m.groupValues[2].toLongOrNull() ?: continue
                val third = m.groupValues[3]
                // 2 段 [分:秒]：first=分、second=秒；3 段 [时:分:秒]：first=时、second=分、third=秒
                val min: Long
                val sec: Long
                if (third.isEmpty()) {
                    min = first
                    sec = second
                } else {
                    sec = third.toLongOrNull() ?: continue
                    min = first * 60 + second
                }
                val frac = m.groupValues[4]
                // 对齐上游 v1.9.3 LyricPlayer 的时间标签修复：非毫秒制下小数部分按百分秒
                // 补齐（[00:01.4] 的 .4 表示 40ms，旧实现 *100 得到 400ms，时间整体偏慢）。
                // 1/2 位小数统一 *10；3 位已是毫秒直接用；超过 3 位按上游取前 3 位。
                val ms = when (frac.length) {
                    0 -> 0L
                    1, 2 -> frac.toLong() * 10
                    else -> frac.take(3).toLong()
                }
                // offset 为正且歌词靠前时，ms-offset 可能为负，会导致第 0 秒就高亮首行；钳到 0
                lines.add(LyricLine((min * 60_000 + sec * 1000 + ms - offset).coerceAtLeast(0L), text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
