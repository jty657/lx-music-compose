package com.lxmusic.mobile.core.player

/** LRC 歌词解析（对应原版 utils/lrcTools.ts 的基础解析） */
object LrcParser {

    /** 时间标签，如 [01:23.45] 或 [01:23.456] 或 [1:23] */
    private val TIME_TAG = Regex("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]")

    /**
     * 解析普通 LRC。多时间标签行会展开成多行。
     * 返回按时间升序排列的歌词行；无有效时间标签时返回空列表。
     */
    fun parse(lrc: String?): List<LyricLine> {
        if (lrc.isNullOrBlank()) return emptyList()
        val lines = mutableListOf<LyricLine>()
        for (raw in lrc.split('\n', '\r')) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            val matches = TIME_TAG.findAll(line).toList()
            if (matches.isEmpty()) continue
            val text = line.substring(matches.last().range.last + 1).trim()
            if (text.isEmpty()) continue
            for (m in matches) {
                val min = m.groupValues[1].toLongOrNull() ?: continue
                val sec = m.groupValues[2].toLongOrNull() ?: continue
                val frac = m.groupValues[3]
                val ms = when (frac.length) {
                    0 -> 0L
                    1 -> frac.toLong() * 100
                    2 -> frac.toLong() * 10
                    else -> frac.take(3).toLong()
                }
                lines.add(LyricLine(min * 60_000 + sec * 1000 + ms, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}
