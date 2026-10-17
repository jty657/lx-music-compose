package com.lingxu.music.core.repository

import com.lingxu.music.core.db.AppDatabase
import com.lingxu.music.core.db.Playlist
import com.lingxu.music.core.db.PlaylistMusic
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.SongListInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 我的列表仓库：对应原版「我的列表」（utils/data.ts 的 @user_list + @list__{id}）。
 * - 本地列表：纯用户创建，存在本 JSON 文件
 * - 在线列表：收藏平台歌单/榜单得到，带 source + sourceListId，可同步远端内容
 */
class MyListsRepository(private val db: AppDatabase) {

    private val _lists = MutableStateFlow<List<Playlist>>(emptyList())
    val lists: StateFlow<List<Playlist>> = _lists.asStateFlow()

    /** 当前激活列表 id（对应原版 list_prev_select_id） */
    private val _activeListId = MutableStateFlow(LOVE_ID)
    val activeListId: StateFlow<String> = _activeListId.asStateFlow()

    private val mutex = Mutex()
    private var loaded = false

    private suspend fun ensureLoaded() {
        if (!loaded) {
            _lists.value = db.loadPlaylists()
            loaded = true
        }
    }

    suspend fun load(): List<Playlist> = mutex.withLock {
        ensureLoaded()
        _lists.value
    }

    /** 外部（如备份恢复）改动后强制重读：否则下一次写操作会 upsert 旧列表并删掉刚恢复的列表 */
    suspend fun reload(): List<Playlist> = mutex.withLock {
        _lists.value = db.loadPlaylists()
        loaded = true
        _lists.value
    }

    fun setActiveList(id: String) {
        _activeListId.value = id
    }


    /** 新建列表（对应原版 createUserList） */
    suspend fun create(name: String): Playlist = mutex.withLock {
        ensureLoaded()
        val list = Playlist(id = "userlist_${System.currentTimeMillis()}", name = name, createTime = System.currentTimeMillis())
        persist(_lists.value + list)
        list
    }

    /** 重命名（对应原版 updateUserList） */
    suspend fun rename(id: String, name: String) = mutex.withLock {
        ensureLoaded()
        persist(_lists.value.map { if (it.id == id) it.copy(name = name, locationUpdateTime = System.currentTimeMillis()) else it })
    }

    /** 删除列表（对应原版 removeUserList） */
    suspend fun remove(id: String) = mutex.withLock {
        ensureLoaded()
        persist(_lists.value.filterNot { it.id == id })
        db.deletePlaylistMusics(id)
        if (_activeListId.value == id) _activeListId.value = LOVE_ID
        Unit
    }

    /** 列表排序（对应原版 updateUserListPosition） */
    suspend fun move(id: String, position: Int) = mutex.withLock {
        ensureLoaded()
        val current = _lists.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index < 0) return@withLock
        val item = current.removeAt(index)
        current.add(position.coerceIn(0, current.size), item.copy(locationUpdateTime = System.currentTimeMillis()))
        persist(current)
    }

    /** 收藏平台歌单到「我的列表」（对应原版 collectList） */
    suspend fun collectSongList(info: SongListInfo): Playlist = mutex.withLock {
        ensureLoaded()
        val existing = _lists.value.firstOrNull { it.sourceListId == info.id && it.source == info.source }
        if (existing != null) return@withLock existing
        val list = Playlist(
            id = "userlist_${System.currentTimeMillis()}",
            name = info.name,
            createTime = System.currentTimeMillis(),
            source = info.source,
            sourceListId = info.id,
            locationUpdateTime = System.currentTimeMillis(),
        )
        persist(_lists.value + list)
        list
    }

    /** 收藏榜单到「我的列表」（对应原版 board__ 前缀约定） */
    suspend fun collectBoard(source: String, bangId: String, name: String): Playlist = mutex.withLock {
        ensureLoaded()
        val sourceListId = "board__${source}__$bangId"
        val existing = _lists.value.firstOrNull { it.sourceListId == sourceListId }
        if (existing != null) return@withLock existing
        val list = Playlist(
            id = "userlist_${System.currentTimeMillis()}",
            name = name,
            createTime = System.currentTimeMillis(),
            source = source,
            sourceListId = sourceListId,
            locationUpdateTime = System.currentTimeMillis(),
        )
        persist(_lists.value + list)
        list
    }

    suspend fun getMusics(listId: String): List<MusicInfo> =
        db.getPlaylistMusics(listId).sortedBy { it.sort }.map { it.music }

    /**
     * 覆盖写入在线列表的同步结果并刷新更新时间（对应原版 core/syncSourceList.ts）
     */
    suspend fun overwriteOnlineMusics(listId: String, musics: List<MusicInfo>): Int = mutex.withLock {
        if (musics.isEmpty()) return@withLock 0
        saveMusicsLocked(listId, musics)
        persist(_lists.value.map { if (it.id == listId) it.copy(locationUpdateTime = System.currentTimeMillis()) else it })
        musics.size
    }

    suspend fun saveMusics(listId: String, musics: List<MusicInfo>) = mutex.withLock {
        saveMusicsLocked(listId, musics)
    }

    /** 不带锁的实现：调用方必须已持有 mutex（Mutex 不可重入） */
    private suspend fun saveMusicsLocked(listId: String, musics: List<MusicInfo>) {
        ensureLoaded()
        val existing = db.getPlaylistMusics(listId)
        val items = musics.mapIndexed { index, music ->
            val old = existing.firstOrNull { it.id == music.id }
            if (old != null) old.copy(sort = index, listId = listId) else PlaylistMusic(id = music.id, music = music, sort = index, listId = listId)
        }
        db.savePlaylistMusics(listId, items)
        persist(_lists.value.map { if (it.id == listId) it.copy(musics = musics.map { it.id }) else it })
    }

    /** 追加歌曲（'top' 置顶，否则追加到末尾；按 id 去重，对应原版 listMusicAdd） */
    suspend fun addMusics(listId: String, musics: List<MusicInfo>, location: String = "bottom") = mutex.withLock {
        ensureLoaded()
        val current = getMusics(listId).toMutableList()
        val existingIds = current.map { it.id }.toSet()
        val fresh = musics.filterNot { it.id in existingIds }
        if (location == "top") current.addAll(0, fresh) else current.addAll(fresh)
        saveMusicsLocked(listId, current)
    }

    suspend fun removeMusics(listId: String, ids: List<String>) = mutex.withLock {
        ensureLoaded()
        val next = getMusics(listId).filterNot { it.id in ids }
        saveMusicsLocked(listId, next)
    }

    suspend fun clearMusics(listId: String) = mutex.withLock {
        ensureLoaded()
        saveMusicsLocked(listId, emptyList())
    }

    /** 列表内歌曲移动位置（对应原版 updateListMusicPosition） */
    suspend fun moveMusic(listId: String, id: String, position: Int) = mutex.withLock {
        ensureLoaded()
        val current = getMusics(listId).toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index < 0) return@withLock
        val item = current.removeAt(index)
        current.add(position.coerceIn(0, current.size), item)
        saveMusicsLocked(listId, current)
    }

    private suspend fun persist(list: List<Playlist>) {
        _lists.value = list
        list.forEach { db.upsertPlaylist(it) }
        val removed = db.loadPlaylists().filterNot { it.id in list.map { l -> l.id } }
        removed.forEach { db.deletePlaylist(it.id) }
    }

    companion object {
        /** 固定列表 id，对齐原版 LIST_IDS */
        const val LOVE_ID = "love"
        const val DEFAULT_ID = "default"
    }
}
