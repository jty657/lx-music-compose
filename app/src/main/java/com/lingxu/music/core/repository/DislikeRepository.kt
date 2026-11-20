package com.lingxu.music.core.repository

import android.content.Context
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    /** 读写都在同一临界区，避免 load 未完成时 add 用空规则覆盖磁盘已有数据、以及并发 add 丢更新 */
    private val mutex = Mutex()
    private var loaded = false

    /** 从磁盘读取一次（调用方需持有 mutex 或确保单线程初始化） */
    private fun loadLocked() {
        _rules.value = if (file.exists()) {
            val text = runCatching { file.readText() }.getOrNull()
            val parsed = text?.let { t -> runCatching { json.decodeFromString<List<String>>(t) }.getOrNull() }
            if (parsed != null) {
                parsed
            } else {
                // 损坏文件隔离：解析失败时改名保留现场，绝不原地留着——否则下次 add/persist
                // 会把「空规则 + 新规则」写回，用户原有不喜欢列表无声丢失（与 AppDatabase 同类修复）
                runCatching {
                    file.renameTo(java.io.File(file.parentFile, "${file.name}.corrupt.${System.currentTimeMillis()}"))
                }
                emptyList()
            }
        } else emptyList()
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            loadLocked()
            loaded = true
        }
    }

    suspend fun add(rule: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            // 冷启动时 load() 走异步 IO，用户可能在此之前就添加规则；先补一次读盘，
            // 否则会用「空列表 + 新规则」覆盖磁盘已有规则（与 FavoritesRepository 守卫一致）
            if (!loaded) {
                loadLocked()
                loaded = true
            }
            val trimmed = rule.trim()
            if (trimmed.isEmpty() || trimmed == "@") return@withLock
            persist((_rules.value + trimmed).distinct())
        }
    }

    suspend fun remove(rule: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!loaded) {
                loadLocked()
                loaded = true
            }
            persist(_rules.value.filterNot { it == rule })
        }
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
