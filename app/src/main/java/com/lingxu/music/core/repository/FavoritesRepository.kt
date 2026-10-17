package com.lingxu.music.core.repository

import com.lingxu.music.core.db.AppDatabase
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 我喜欢（收藏）仓库。
 * 对应原版「我的列表 → 我喜欢」，存储于 JSON 文件（filesDir/favorites.json）。
 */
class FavoritesRepository(private val db: AppDatabase) {

    private val _favorites = MutableStateFlow<List<MusicInfo>>(emptyList())
    val favorites: StateFlow<List<MusicInfo>> = _favorites.asStateFlow()

    val favoriteIds: Flow<Set<String>> = favorites.map { list -> list.map { it.id }.toSet() }

    /** Mutex 非重入，读写收藏都在同一临界区内完成 */
    private val mutex = Mutex()

    private var loaded = false

    suspend fun load(): List<MusicInfo> = mutex.withLock {
        if (!loaded) {
            _favorites.value = db.loadFavorites()
            loaded = true
        }
        _favorites.value
    }

    /** 外部（如备份恢复）改动后强制重读：否则下一次写操作会用旧缓存覆盖刚恢复的数据 */
    suspend fun reload(): List<MusicInfo> = mutex.withLock {
        _favorites.value = db.loadFavorites()
        loaded = true
        _favorites.value
    }

    fun isFavorite(id: String): Boolean = _favorites.value.any { it.id == id }

    /** 切换收藏状态，返回切换后是否已收藏 */
    suspend fun toggle(musicInfo: MusicInfo): Boolean = mutex.withLock {
        if (!loaded) {
            _favorites.value = db.loadFavorites()
            loaded = true
        }
        val current = _favorites.value
        val exists = current.any { it.id == musicInfo.id }
        val next = if (exists) {
            current.filterNot { it.id == musicInfo.id }
        } else {
            listOf(musicInfo) + current
        }
        db.saveFavorites(next)
        _favorites.value = next
        !exists
    }

    suspend fun remove(id: String) = mutex.withLock {
        if (!loaded) {
            _favorites.value = db.loadFavorites()
            loaded = true
        }
        val next = _favorites.value.filterNot { it.id == id }
        db.saveFavorites(next)
        _favorites.value = next
        Unit
    }

    suspend fun clear() = mutex.withLock {
        db.saveFavorites(emptyList())
        _favorites.value = emptyList()
        Unit
    }
}
