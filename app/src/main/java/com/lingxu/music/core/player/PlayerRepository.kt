package com.lingxu.music.core.player

import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lingxu.music.core.model.LyricInfo
import com.lingxu.music.core.model.MusicInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** 播放模式（原版 player.togglePlayMethod） */
object PlayMode {
    const val LIST_LOOP = "listLoop"
    const val RANDOM = "random"
    const val LIST = "list"
    const val SINGLE_LOOP = "singleLoop"
    const val SINGLE = "single"

    val ALL = listOf(LIST_LOOP, RANDOM, LIST, SINGLE_LOOP, SINGLE)

    fun label(mode: String): String = when (mode) {
        LIST_LOOP -> "列表循环"
        RANDOM -> "随机播放"
        LIST -> "顺序播放"
        SINGLE_LOOP -> "单曲循环"
        SINGLE -> "单曲播放"
        else -> "列表循环"
    }
}

/** 歌词行（translation 为翻译歌词，同时间点合并） */
/** 歌词行 */
@kotlinx.serialization.Serializable
data class LyricLine(
    val timeMs: Long,
    val text: String,
    val translation: String? = null,
    val roma: String? = null,
)

/** 播放状态 */
@kotlinx.serialization.Serializable
data class PlayState(
    val current: MusicInfo? = null,
    val queue: List<MusicInfo> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: String = PlayMode.LIST_LOOP,
    val lyricLines: List<LyricLine> = emptyList(),
    val coverUrl: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    /** 定时停止播放剩余秒数，-1 表示未开启 */
    val timeoutExitRemaining: Int = -1,
) {
    /** 当前应高亮的歌词行下标 */
    val currentLyricIndex: Int
        get() {
            if (lyricLines.isEmpty()) return -1
            var idx = -1
            for (i in lyricLines.indices) {
                if (lyricLines[i].timeMs <= positionMs) idx = i else break
            }
            return idx
        }
}

/**
 * 播放仓库：Media3 ExoPlayer + 播放队列管理。
 * 对应原版 react-native-track-player + core/player/player.ts 的职责。
 */
class PlayerRepository(context: Context) {

    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .build()

    /**
     * 音频焦点处理开关（设置项 player.isHandleAudioFocus）。
     * 关闭时来电或其它 App 播放不会让本 App 暂停。
     */
    fun setHandleAudioFocus(enabled: Boolean) {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            enabled,
        )
    }

    /** 播放倍速（设置项 player.playbackRate） */
    fun setPlaybackRate(rate: Float) {
        player.setPlaybackSpeed(rate.coerceIn(0.5f, 3f))
    }

    /** 音量（设置项 player.volume），1f 为原始音量 */
    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    // ============ 定时停止播放（对应原版 core/player/timeoutExit.ts）============

    private var timeoutJob: Job? = null

    /** 是否正在等待当前歌曲播完再停 */
    private var pendingTimeoutStop = false

    /**
     * 启动定时停止播放。
     * @param minutes 倒计时分钟数，<=0 表示取消
     * @param waitForSongEnd 为 true 且正在播放时，等当前歌曲播完再停（原 timeoutExitPlayed）
     */
    fun startTimeoutExit(minutes: Int, waitForSongEnd: Boolean) {
        timeoutJob?.cancel()
        pendingTimeoutStop = false
        if (minutes <= 0) {
            _state.update { it.copy(timeoutExitRemaining = -1) }
            return
        }
        _state.update { it.copy(timeoutExitRemaining = minutes * 60) }
        timeoutJob = scope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _state.update { it.copy(timeoutExitRemaining = remaining) }
            }
            if (waitForSongEnd && _state.value.isPlaying) {
                // 等待当前歌曲播完：置标志，切歌时再停
                pendingTimeoutStop = true
                _state.update { it.copy(timeoutExitRemaining = -1) }
            } else {
                player.pause()
                _state.update { it.copy(timeoutExitRemaining = -1) }
            }
        }
    }

    fun cancelTimeoutExit() {
        timeoutJob?.cancel()
        timeoutJob = null
        pendingTimeoutStop = false
        _state.update { it.copy(timeoutExitRemaining = -1) }
    }

    /** 持久化回调（由 App 注入）：切歌/暂停时保存，供启动时恢复 */
    var lastPlaySaver: (suspend (PlayState) -> Unit)? = null

    /** 保存当前播放状态（对应原版 player.isSavePlayTime + 上次播放记忆） */
    fun saveLastPlay() {
        val state = _state.value
        if (state.current == null) return
        scope.launch { lastPlaySaver?.invoke(state) }
    }

    /**
     * 恢复上次播放的队列（不自动播放）。
     * @param autoPlay 为 true 时直接开始播放（对应设置项 player.startupAutoPlay）
     */
    fun restoreLastPlay(saved: PlayState, autoPlay: Boolean, startPositionMs: Long = 0L) {
        if (saved.queue.isEmpty() || saved.current == null) return
        val index = saved.queueIndex.coerceIn(0, saved.queue.lastIndex)
        // 只恢复队列相关字段：不能用整份持久化状态覆盖（会把 playMode/error/coverUrl 等一起带回旧值，
        // 例如与刚恢复的 settings.playMode 不一致）
        _state.update {
            it.copy(
                queue = saved.queue,
                queueIndex = index,
                current = saved.queue[index],
                positionMs = startPositionMs,
                durationMs = 0L,
                isPlaying = false,
                loading = false,
                lyricLines = emptyList(),
                error = null,
            )
        }
        player.setMediaItems(saved.queue.map { it.toMediaItem() }, index, startPositionMs)
        player.prepare()
        if (autoPlay) player.play()
        loadLyric(saved.queue[index])
        loadCover(saved.queue[index])
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var lyricJob: Job? = null
    private var coverJob: Job? = null

    /** 歌词加载器（由 App 注入，避免 core 层反向依赖） */
    var lyricLoader: (suspend (MusicInfo) -> LyricInfo?)? = null

    /** 封面加载器（由 App 注入） */
    var coverLoader: (suspend (MusicInfo) -> String?)? = null

    /**
     * 播放信息加载器：拿到可播放的 URL 后填回 meta.filePath（由 App 注入）。
     * 播放前调用，避免各处重复写 getMusicUrl + copy 逻辑。
     */
    var playInfoLoader: (suspend (MusicInfo) -> MusicInfo)? = null

    /** 播放模式持久化回调（由 App 注入） */
    var onPlayModeChange: ((String) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        loading = playbackState == Player.STATE_BUFFERING,
                        error = null,
                    )
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.update { it.copy(error = error.message ?: "播放失败", loading = false) }
            }

            /** 切歌时同步当前曲目与下标（手动、自动、循环、随机都走这里） */
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // 定时停止：等当前歌曲播完再停模式下，切歌即暂停
                // 单曲循环下重播的 reason 是 REPEAT 而不是 AUTO，漏掉会让定时停止永不生效
                if (pendingTimeoutStop &&
                    (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT)
                ) {
                    pendingTimeoutStop = false
                    player.pause()
                }
                val index = player.currentMediaItemIndex
                val current = _state.value.queue.getOrNull(index)
                // 自动/循环转场说明上层本就在播；不能只看 isPlaying（转场瞬间可能已是 false）
                val shouldPlay = reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT ||
                    _state.value.isPlaying
                // 单曲播放：放完当前这首就停（原版 'single' 在 getNextPlayMusicInfo 落到 default → null）
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                    _state.value.playMode == PlayMode.SINGLE
                ) {
                    player.pause()
                }
                _state.update {
                    it.copy(
                        queueIndex = index,
                        current = current,
                        positionMs = 0L,
                        durationMs = 0L,
                        lyricLines = emptyList(),
                    )
                }
                current?.let { loadLyric(it) }
                current?.let { loadCover(it) }
                if (current != null && current.meta.filePath.isNullOrBlank()) {
                    // 队列是按需解析 URL 的：跳到尚未解析的歌（用户快速切歌）时先补解析再继续，
                    // 否则播放器会拿着空 URI 直接报错。
                    player.pause()
                    scope.launch {
                        val resolved = runCatching { resolvePlayInfo(current) }.getOrDefault(current)
                        if (resolved.meta.filePath.isNullOrBlank()) {
                            _state.update { it.copy(error = "获取播放链接失败，可尝试换源", loading = false) }
                            return@launch
                        }
                        _state.update { st ->
                            val q = st.queue.toMutableList()
                            if (index in q.indices && q[index].id == resolved.id) {
                                q[index] = resolved
                                st.copy(queue = q, current = resolved, error = null)
                            } else st
                        }
                        runCatching {
                            player.replaceMediaItem(index, resolved.toMediaItem())
                            player.prepare()
                            if (shouldPlay) player.play()
                        }
                    }
                } else {
                    // 预取下一首，让「下一首」几乎立即出声
                    ensureResolved(index + 1)
                }
            }
        })

        scope.launch {
            while (isActive) {
                runCatching {
                    val duration = player.duration
                    _state.update {
                        it.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0L),
                            durationMs = if (duration > 0) duration else 0L,
                        )
                    }
                }
                delay(500)
            }
        }
    }

    /**
     * 播放指定队列。
     *
     * 只解析「当前这首」再开播，然后后台预取下一首；其余保持未解析、切到时再按需解析。
     * 原先各列表页会先 `queue.map { resolvePlayInfo(it) }` 串行解析整队列
     * （30 首歌 = 30 次请求、且可能触发换源搜索），点一下要等很久才开始出声。
     */
    fun playQueue(queue: List<MusicInfo>, index: Int) {
        if (queue.isEmpty()) return
        val safeIndex = index.coerceIn(0, queue.lastIndex)
        scope.launch {
            val target = queue[safeIndex]
            val resolved = if (target.meta.filePath.isNullOrBlank()) {
                _state.update { it.copy(loading = true) }
                val r = runCatching { resolvePlayInfo(target) }.getOrDefault(target)
                _state.update { it.copy(loading = false) }
                r
            } else target
            if (resolved.meta.filePath.isNullOrBlank()) {
                // 解析失败时不要动 state/播放器：否则界面会显示一首其实没在播的歌，
                // 后续 playAt/next/prev 会拿新队列下标去操作旧播放列表
                _state.update { it.copy(error = "获取播放链接失败，可尝试换源") }
                return@launch
            }
            val updated = queue.toMutableList().also { it[safeIndex] = resolved }
            _state.update {
                it.copy(
                    queue = updated,
                    queueIndex = safeIndex,
                    current = resolved,
                    lyricLines = emptyList(),
                    positionMs = 0L,
                    durationMs = 0L,
                    error = null,
                )
            }
            player.setMediaItems(updated.map { it.toMediaItem() }, safeIndex, 0L)
            player.prepare()
            player.play()
            loadLyric(resolved)
            saveLastPlay()
            ensureResolved(safeIndex + 1)
        }
    }

    /** 正在按需解析的队列下标，避免同一首重复发请求 */
    private val resolvingIndexes = mutableSetOf<Int>()

    /** 后台解析并回填队列中第 index 首的播放 URL（不打断当前播放） */
    private fun ensureResolved(index: Int) {
        val info = _state.value.queue.getOrNull(index) ?: return
        if (!info.meta.filePath.isNullOrBlank()) return
        if (!resolvingIndexes.add(index)) return
        scope.launch {
            val resolved = runCatching { resolvePlayInfo(info) }.getOrDefault(info)
            resolvingIndexes.remove(index)
            if (resolved.meta.filePath.isNullOrBlank()) return@launch
            _state.update { st ->
                val q = st.queue.toMutableList()
                if (index in q.indices && q[index].id == info.id) {
                    q[index] = resolved
                    st.copy(queue = q, current = if (index == st.queueIndex) resolved else st.current)
                } else st
            }
            runCatching { player.replaceMediaItem(index, resolved.toMediaItem()) }
        }
    }

    /** 播放队列中第 index 首 */
    fun playAt(index: Int) {
        val queue = _state.value.queue
        if (index !in queue.indices) return
        if (queue[index].meta.filePath.isNullOrBlank()) {
            // 尚未解析：先解析再开播，否则会拿空 URI 先报一次源错误
            playQueue(queue, index)
            return
        }
        player.seekTo(index, 0L)
        player.prepare()
        player.play()
        _state.update { it.copy(queueIndex = index, current = queue[index], positionMs = 0L) }
        loadLyric(queue[index])
    }

    /**
     * 从队列移除第 index 首。
     * 移除当前项时跳到同下标（即原来的下一首）；否则只从播放列表里摘掉该项，
     * 保住当前位置与播放状态（原先无条件 setMediaItems(..., 0L) 会让正在放的歌跳回开头）。
     */
    fun removeFromQueue(index: Int) {
        val state = _state.value
        if (index !in state.queue.indices) return
        val next = state.queue.filterIndexed { i, _ -> i != index }
        if (next.isEmpty()) {
            clear()
            return
        }
        val wasCurrent = index == state.queueIndex
        val newIndex = when {
            wasCurrent -> index.coerceAtMost(next.lastIndex)
            index < state.queueIndex -> state.queueIndex - 1
            else -> state.queueIndex
        }
        if (wasCurrent) {
            playQueue(next, newIndex)
        } else {
            _state.update { it.copy(queue = next, queueIndex = newIndex) }
            runCatching { player.removeMediaItem(index) }
        }
    }

    /** 播放单曲（替换整个队列） */
    fun playSingle(musicInfo: MusicInfo) = playQueue(listOf(musicInfo), 0)

    /** 队列内移动位置（上移/下移排序） */
    fun moveInQueue(from: Int, to: Int) {
        val state = _state.value
        if (from !in state.queue.indices || to !in state.queue.indices || from == to) return
        val list = state.queue.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        val newCurrent = when {
            state.queueIndex == from -> to
            from < state.queueIndex && to >= state.queueIndex -> state.queueIndex - 1
            from > state.queueIndex && to <= state.queueIndex -> state.queueIndex + 1
            else -> state.queueIndex
        }
        _state.update { it.copy(queue = list, queueIndex = newCurrent) }
        // 同步播放器列表；用当前播放位置重建，避免正在放的歌回到 0 秒
        runCatching {
            val position = player.currentPosition.coerceAtLeast(0L)
            val playWhenReady = player.playWhenReady
            player.setMediaItems(list.map { it.toMediaItem() }, newCurrent, position)
            player.prepare()
            player.playWhenReady = playWhenReady
        }
    }

    /**
     * 解析播放信息：为队列中每首歌取可播放 URL 并写回 meta.filePath（由 App 注入 playInfoLoader）。
     * 取不到的歌曲保持原样，播放器会自然报错而不是崩溃。
     */
    suspend fun resolvePlayInfo(musicInfo: MusicInfo): MusicInfo {
        val loader = playInfoLoader ?: return musicInfo
        return runCatching { loader(musicInfo) }.getOrDefault(musicInfo)
    }

    /** 追加到队列（新加入的项可能还没解析 URL，后台补上） */
    fun addToQueue(musicInfo: MusicInfo) {
        val next = _state.value.queue + musicInfo
        _state.update { it.copy(queue = next) }
        player.addMediaItem(musicInfo.toMediaItem())
        ensureResolved(next.lastIndex)
    }

    fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
        saveLastPlay()
    }

    fun next() {
        if (_state.value.queue.isEmpty()) return
        // 手动切歌在队尾/队首要回绕（原版 playNext/playPrev 会把 single/list 重映射成 listLoop）
        if (player.shuffleModeEnabled) {
            if (player.hasNextMediaItem()) player.seekToNextMediaItem() else player.seekTo(0, 0L)
        } else {
            val last = _state.value.queue.lastIndex
            val target = if (player.currentMediaItemIndex < last) player.currentMediaItemIndex + 1 else 0
            player.seekTo(target, 0L)
        }
        saveLastPlay()
    }

    fun prev() {
        if (_state.value.queue.isEmpty()) return
        if (player.shuffleModeEnabled) {
            if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(_state.value.queue.lastIndex, 0L)
        } else {
            val last = _state.value.queue.lastIndex
            val target = if (player.currentMediaItemIndex > 0) player.currentMediaItemIndex - 1 else last
            player.seekTo(target, 0L)
        }
        saveLastPlay()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    /** 切换播放模式并持久化 */
    fun setPlayMode(mode: String) {
        applyPlayMode(mode)
        onPlayModeChange?.invoke(mode)
    }

    /** 启动时恢复已保存的播放模式（不触发持久化） */
    fun restorePlayMode(mode: String) {
        applyPlayMode(mode)
    }

    private fun applyPlayMode(mode: String) {
        _state.update { it.copy(playMode = mode) }
        when (mode) {
            PlayMode.SINGLE_LOOP -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }
            PlayMode.LIST_LOOP -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = false
            }
            // 随机播放此前只设了 REPEAT_MODE_ALL，播放顺序仍是顺序的
            PlayMode.RANDOM -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }
            // LIST（顺序播放）与 SINGLE（单曲播放，播完停）都关闭循环；单曲播放的「停」在转场回调里处理
            else -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
            }
        }
        player.shuffleModeEnabled = mode == PlayMode.RANDOM
    }

    fun clear() {
        player.stop()
        player.clearMediaItems()
        lyricJob?.cancel()
        coverJob?.cancel()
        _state.update {
            PlayState(playMode = it.playMode)
        }
    }

    fun release() {
        lyricJob?.cancel()
        player.release()
    }

    /** 加载当前歌曲歌词（含翻译/逐字，与原版 player.isShowLyricTranslation 一致） */
    private fun loadLyric(musicInfo: MusicInfo) {
        val loader = lyricLoader ?: return
        lyricJob?.cancel()
        lyricJob = scope.launch {
            val info = runCatching { loader(musicInfo) }.getOrNull()
            // 若期间已切歌则丢弃结果
            if (_state.value.current?.id != musicInfo.id) return@launch
            _state.update { it.copy(lyricLines = LrcParser.parse(info)) }
        }
    }

    /** 加载当前歌曲封面（优先搜索结果的 meta.picUrl，否则经 coverLoader 获取） */
    private fun loadCover(musicInfo: MusicInfo) {
        coverJob?.cancel()
        val url = musicInfo.meta.picUrl
        if (url != null) {
            _state.update { it.copy(coverUrl = url) }
            return
        }
        val loader = coverLoader ?: return
        coverJob = scope.launch {
            val result = runCatching { loader(musicInfo) }.getOrNull()
            // 若期间已切歌则丢弃结果
            if (_state.value.current?.id != musicInfo.id) return@launch
            _state.update { it.copy(coverUrl = result) }
        }
    }

    private fun MusicInfo.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(meta.filePath ?: "")
            .setMediaId(id)
            // 不设 metadata 时，系统媒体通知/锁屏只会显示空白（无歌名、歌手、封面）
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(name)
                    .setArtist(singer)
                    .setAlbumTitle(meta.albumName.ifBlank { null })
                    .setArtworkUri(meta.picUrl?.let { android.net.Uri.parse(it) })
                    .build(),
            )
            .build()

    companion object {
        @Volatile
        private var instance: PlayerRepository? = null

        fun get(context: Context): PlayerRepository =
            instance ?: synchronized(this) {
                instance ?: PlayerRepository(context.applicationContext).also { instance = it }
            }
    }
}

/** 播放服务（前台服务，用于后台播放） */
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = PlayerRepository.get(this).player
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        // player 由 PlayerRepository 单例持有，这里只释放 MediaSession
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
