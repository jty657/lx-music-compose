package com.lxmusic.mobile.core.repository

import com.lxmusic.mobile.core.db.AppDatabase
import com.lxmusic.mobile.core.model.MusicInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * 我喜欢（收藏）仓库。
 * 对应原版「我的列表 → 我喜欢」，存储于 JSON 文件（filesDir/favorites.json）。
 */
class FavoritesRepository(private val db: AppDatabase) {

    private val _favorites = MutableStateFlow<List<MusicInfo>>(emptyList())
    val favorites: StateFlow<List<MusicInfo>> = _favorites.asStateFlow()

    val favoriteIds: Flow<Set<String>> = favorites.map { list -> list.map { it.id }.toSet() }

    private var loaded = false

    suspend fun load(): List<MusicInfo> {
        if (loaded) return _favorites.value
        val list = db.loadFavorites()
        _favorites.value = list
        loaded = true
        return list
    }

    fun isFavorite(id: String): Boolean = _favorites.value.any { it.id == id }

    /** 切换收藏状态，返回切换后是否已收藏 */
    suspend fun toggle(musicInfo: MusicInfo): Boolean {
        load()
        val current = _favorites.value
        val exists = current.any { it.id == musicInfo.id }
        val next = if (exists) {
            current.filterNot { it.id == musicInfo.id }
        } else {
            listOf(musicInfo) + current
        }
        db.saveFavorites(next)
        _favorites.value = next
        return !exists
    }

    suspend fun remove(id: String) {
        load()
        val next = _favorites.value.filterNot { it.id == id }
        db.saveFavorites(next)
        _favorites.value = next
    }

    suspend fun clear() {
        db.saveFavorites(emptyList())
        _favorites.value = emptyList()
    }
}
