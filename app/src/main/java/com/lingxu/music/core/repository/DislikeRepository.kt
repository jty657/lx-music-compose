package com.lingxu.music.core.repository

import android.content.Context
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 不喜欢列表（对应原版 core/dislikeList.ts）。
 *
 * 规则格式：
 * - `歌曲名@歌手`：名称与歌手都匹配才命中
 * - `歌曲名`：只匹配名称
 * - `@歌手`：只匹配歌手
 * 命中的歌曲在切歌时会被自动跳过。
 */
class DislikeRepository(private val context: Context) {

    private val file = java.io.File(context.filesDir, "dislike_list.json")
    private val json = Json { ignoreUnknownKeys = true }

    private val _rules = MutableStateFlow<List<String>>(emptyList())
    val rules: StateFlow<List<String>> = _rules.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        _rules.value = runCatching {
            json.decodeFromString<List<String>>(file.readText())
        }.getOrDefault(emptyList())
        Unit
    }

    suspend fun add(rule: String) = withContext(Dispatchers.IO) {
        val trimmed = rule.trim()
        if (trimmed.isEmpty() || trimmed == "@") return@withContext
        persist((_rules.value + trimmed).distinct())
    }

    suspend fun remove(rule: String) = withContext(Dispatchers.IO) {
        persist(_rules.value.filterNot { it == rule })
    }

    fun isDisliked(music: MusicInfo): Boolean {
        val name = music.name
        val singer = music.singer
        return _rules.value.any { matches(it, name, singer) }
    }

    private fun persist(list: List<String>) {
        _rules.value = list
        runCatching {
            val tmp = java.io.File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(json.encodeToString(list))
            if (!tmp.renameTo(file)) {
                file.writeText(json.encodeToString(list))
                tmp.delete()
            }
        }
    }

    private fun matches(rule: String, name: String, singer: String): Boolean {
        val sep = rule.indexOf('@')
        return when {
            sep < 0 -> name == rule
            sep == 0 -> singer == rule.substring(1)
            else -> name == rule.substring(0, sep) && singer == rule.substring(sep + 1)
        }
    }
}
