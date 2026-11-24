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
        // 文案对齐原版 lang/zh-cn.json（play_list_loop / play_single_loop / play_single）
        LIST_LOOP -> "列表循环播放"
        RANDOM -> "随机播放"
        LIST -> "顺序播放"
        SINGLE_LOOP -> "单曲循环播放"
        SINGLE -> "禁用歌曲切换"
        else -> "列表循环播放"
    }
}

/** 歌词行（translation 为翻译歌词，roma 为罗马音，同时间点合并） */
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
    /** 当前应高亮的歌词行下标（二分查找：进度每 500ms 更新一次，歌词可达数百行，避免逐行线性扫描） */
    val currentLyricIndex: Int
        get() {
            if (lyricLines.isEmpty()) return -1
            var low = 0
            var high = lyricLines.lastIndex
            var found = -1
            while (low <= high) {
                val mid = (low + high) ushr 1
                if (lyricLines[mid].timeMs <= positionMs) {
                    found = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            return found
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

    /**
     * 定时退出到点时的「退出应用」回调（由 MainActivity 注入）。
     * 对齐原版 timeoutExit.exit()：未开启「播完当前曲再退」时，到点直接退出应用，
     * 而不是只暂停播放。
     */
    var onTimeoutExit: (() -> Unit)? = null

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
        // minutes 来自持久化，异常大值做 Long 运算后再钳制，避免 `minutes * 60` 的 Int 溢出
        // 变成负数导致 deadline 立即到期、播放被瞬间暂停。
        val totalSeconds = (minutes.toLong() * 60).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        _state.update { it.copy(timeoutExitRemaining = totalSeconds) }
        // 按绝对到期时间计算剩余秒数，而非「每 delay(1000) 减 1」：
        // App 退到后台被系统冻结后协程 delay 会被显著拉长，累减会让定时严重变慢
        // （设 30 分钟实际可能一小时后仍未到点）；用系统时钟重算可抗挂起漂移。
        val deadline = System.currentTimeMillis() + totalSeconds * 1000L
        timeoutJob = scope.launch {
            while (true) {
                delay(1000)
                val remaining = ((deadline - System.currentTimeMillis()) / 1000L)
                    .coerceAtLeast(0L).toInt()
                _state.update { it.copy(timeoutExitRemaining = remaining) }
                if (remaining <= 0) break
            }
            if (waitForSongEnd && _state.value.isPlaying) {
                // 等待当前歌曲播完：置标志，切歌时再停
                pendingTimeoutStop = true
                _state.update { it.copy(timeoutExitRemaining = -1) }
            } else {
                player.pause()
                _state.update { it.copy(timeoutExitRemaining = -1) }
                // 对齐原版：未开启「播完再退」时到点直接退出应用（而不仅是暂停）
                onTimeoutExit?.invoke()
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

    /** 上次播放状态的在途写盘任务：快速连续切歌时只保留最新一次，避免旧状态最后落盘 */
    private var lastPlayJob: Job? = null

    /** 保存当前播放状态（对应原版 player.isSavePlayTime + 上次播放记忆） */
    fun saveLastPlay() {
        val state = _state.value
        if (state.current == null) return
        // 多次触发（切歌/暂停/转场）都写同一个文件，IO 完成顺序不确定；
        // 取消上一次未完成的写，保证最终落盘的一定是最新状态。
        lastPlayJob?.cancel()
        lastPlayJob = scope.launch { lastPlaySaver?.invoke(state) }
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

    /** 播放出错后的「5 秒自动切下一首」任务（对齐原版 playerEvent.addDelayNextTimeout） */
    private var errorNextJob: Job? = null

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

    /** 不喜欢列表匹配（由 App 注入）：命中则切歌时跳过 */
    var dislikeMatcher: ((MusicInfo) -> Boolean)? = null

    /**
     * 媒体通知/锁屏封面显示开关（原版 player.isShowNotificationImage）。
     * 关闭后通知只显示歌名/歌手，不再加载并显示专辑封面。
     * @Volatile：设置页在 Main 作用域改写，播放解析协程在 IO/其它线程读取，需可见性保证。
     */
    @Volatile
    var showNotificationImage: Boolean = true
        private set

    /**
     * 更新通知封面开关，并即时刷新当前媒体项的 metadata。
     * 用 replaceMediaItem 而非重建队列：它保留当前播放位置与播放/暂停状态，
     * 不触发 onMediaItemTransition，用户切换开关时听感无变化。
     * 仅当当前项已解析出播放地址时才 replace，避免把空 URI 写回导致误报播放错误。
     */
    fun setShowNotificationImage(enabled: Boolean) {
        if (showNotificationImage == enabled) return
        showNotificationImage = enabled
        val index = player.currentMediaItemIndex
        val info = _state.value.queue.getOrNull(index)
        if (info != null && !info.meta.filePath.isNullOrBlank()) {
            runCatching { player.replaceMediaItem(index, info.toMediaItem()) }
        }
    }

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                // 错误提示由 onPlayerError 设置、切歌/重播时清除；不能在这里无条件 error=null，
                // 否则错误发生后紧随的状态回调会瞬间抹掉提示，用户看不到失败原因。
                // 只有真正进入缓冲（开始新的加载）时才清掉上一次的错误。
                _state.update {
                    it.copy(
                        loading = playbackState == Player.STATE_BUFFERING,
                        error = if (playbackState == Player.STATE_BUFFERING) null else it.error,
                    )
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.update { it.copy(error = error.message ?: "播放失败", loading = false) }
                // 对齐原版：音频加载出错 5 秒后自动切下一首（lang 里 player__error 即「5 秒后切换下一首」），
                // 否则某一首取不到播放链接时，播放会永久停在这一首。
                errorNextJob?.cancel()
                errorNextJob = scope.launch {
                    delay(5000)
                    if (_state.value.error != null) {
                        // 先清错误再切歌：next() 会触发 onMediaItemTransition 并取消本协程，
                        // 若把清除放在 next() 之后，错误提示会残留在界面上。
                        _state.update { it.copy(error = null) }
                        next()
                    }
                }
            }

            /** 切歌时同步当前曲目与下标（手动、自动、循环、随机都走这里） */
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // 切歌即视为离开上一首的错误态，取消待执行的「出错自动下一首」
                errorNextJob?.cancel()
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
                // 单曲播放（原版 'none'）：播完当前曲即停，不切下一首。
                // ExoPlayer 在 REPEAT_MODE_OFF 下会自动进入下一项，这里回退到刚播完的曲目再暂停，
                // 否则用户会看到「跳到下一首后暂停」，与原版「停在当前曲」不符。
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                    _state.value.playMode == PlayMode.SINGLE
                ) {
                    player.pause()
                    val prevIndex = player.previousMediaItemIndex
                    if (prevIndex >= 0) {
                        runCatching { player.seekTo(prevIndex, 0L) }
                        return
                    }
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
                // 上次播放要在「state 已切到新曲目」之后保存，否则 next()/prev() 里 seekTo 后
                // 立刻采样只能拿到切歌前的旧曲目，冷启动恢复时就会定位到错误的歌。
                saveLastPlay()
                current?.let { loadLyric(it) }
                current?.let { loadCover(it) }
                // 命中不喜欢列表：跳到下一首未命中的歌（对应原版 dislikeList）
                if (current != null && dislikeMatcher?.invoke(current) == true) {
                    skipDisliked()
                    return
                }
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
                        // 解析期间用户可能已切歌：只有当前下标仍指向这首歌时，才允许改写播放器。
                        // 把 replaceMediaItem/play 也纳入校验，避免把旧歌 URI 塞进新歌的位置导致误播。
                        var stillCurrent = false
                        _state.update { st ->
                            val q = st.queue.toMutableList()
                            if (index in q.indices && q[index].id == resolved.id) {
                                q[index] = resolved
                                stillCurrent = true
                                st.copy(queue = q, current = resolved, error = null)
                            } else st
                        }
                        if (!stillCurrent) return@launch
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

        // 进度轮询：暂停/空闲时停更，避免后台每 500ms 触发全量重组；
        // 恢复播放时立即补一次，保证拖动/跳转后进度不错位。
        var wasPlaying = false
        scope.launch {
            while (isActive) {
                val playing = player.isPlaying
                if (playing) {
                    runCatching {
                        val duration = player.duration
                        _state.update {
                            it.copy(
                                positionMs = player.currentPosition.coerceAtLeast(0L),
                                durationMs = if (duration > 0) duration else 0L,
                            )
                        }
                    }
                } else if (wasPlaying) {
                    // 刚暂停：补一次最终进度后停止更新
                    runCatching {
                        _state.update { it.copy(positionMs = player.currentPosition.coerceAtLeast(0L)) }
                    }
                }
                wasPlaying = playing
                delay(if (playing) 500L else 300L)
            }
        }
    }

    /** 命中不喜欢列表时跳到下一首；整队列一圈都命中则暂停 */
    private fun skipDisliked() {
        val matcher = dislikeMatcher ?: return
        val queue = _state.value.queue
        if (queue.isEmpty()) return
        var index = player.currentMediaItemIndex
        repeat(queue.size) {
            val info = queue.getOrNull(index)
            if (info == null || !matcher(info)) {
                if (index != player.currentMediaItemIndex) player.seekTo(index, 0L)
                return
            }
            index = (index + 1) % queue.size
        }
        // 所有歌都被标记：直接停
        player.pause()
    }

    /** 播放指定队列。
     *
     * 只解析「当前这首」再开播，然后后台预取下一首；其余保持未解析、切到时再按需解析。
     * 原先各列表页会先 `queue.map { resolvePlayInfo(it) }` 串行解析整队列
     * （30 首歌 = 30 次请求、且可能触发换源搜索），点一下要等很久才开始出声。
     */
    /** 最近一次 playQueue 的协程：新的播放请求到来时取消旧的，避免后台慢解析后到覆盖用户最新选择 */
    private var playQueueJob: Job? = null

    /**
     * 播放代号：每次 playQueue/clear/release 都递增。解析 URL 是挂起操作，
     * 期间用户可能清空队列或切到别的歌；在途协程回填前校验代号，过期的直接丢弃，
     * 避免「清空后被旧请求复活」或操作已释放的播放器。
     */
    private var playGeneration: Int = 0

    /**
     * 播放队列。
     * @param autoPlay 是否立即开播；删除当前曲目等场景需要沿用删除前的播放状态，
     *                 避免「暂停时删歌却突然开始播放」。默认 true 保持其它调用方行为不变。
     */
    fun playQueue(queue: List<MusicInfo>, index: Int, autoPlay: Boolean = true) {
        if (queue.isEmpty()) return
        playQueueJob?.cancel()
        val gen = ++playGeneration
        // 在线榜单/歌单可能含重复 id，而队列以 id 作 LazyColumn 稳定 key，重复会抛
        // "Duplicate key" 崩溃。先按 id 去重，再定位目标歌曲去重后的真实下标。
        val target = queue.getOrNull(index) ?: queue[0]
        val distinct = queue.distinctBy { it.id }
        val safeIndex = distinct.indexOfFirst { it.id == target.id }.let { if (it >= 0) it else 0 }
        playQueueJob = scope.launch {
            val resolved = if (target.meta.filePath.isNullOrBlank()) {
                _state.update { it.copy(loading = true) }
                val r = runCatching { resolvePlayInfo(target) }.getOrDefault(target)
                _state.update { it.copy(loading = false) }
                r
            } else target
            // 解析可能耗时（取链/换源），期间用户可能已 clear/切歌；代号变化则丢弃本次结果
            if (gen != playGeneration) return@launch
            if (resolved.meta.filePath.isNullOrBlank()) {
                // 解析失败时不要动 state/播放器：否则界面会显示一首其实没在播的歌，
                // 后续 playAt/next/prev 会拿新队列下标去操作旧播放列表
                _state.update { it.copy(error = "获取播放链接失败，可尝试换源") }
                return@launch
            }
            // 二次校验：更新 loading 到真正写状态之间仍有挂起点（runCatching 内的 loader），
            // 这里再确认一次，确保清空/切歌后的迟到解析不会复活队列。
            if (gen != playGeneration) return@launch
            val updated = distinct.toMutableList().also { it[safeIndex] = resolved }
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
            if (autoPlay) player.play()
            loadLyric(resolved)
            // 此处 state 已切到 resolved，保存是安全的；与转场回调中的保存互为兜底
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
            // 沿用删除前的播放状态：暂停时删当前歌不应突然开始播放，
            // 播放中删当前歌则继续播放下一首（语义与「跳到下一首」一致）。
            playQueue(next, newIndex, autoPlay = player.playWhenReady)
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
        val current = _state.value.queue
        // 队列列表以歌曲 id 作为 LazyColumn 的稳定 key，重复 id 会导致 Compose 抛
        // "Duplicate key" 崩溃；"加入播放队列"会把已在队列中的歌再次加入，故先去重。
        if (current.any { it.id == musicInfo.id }) return
        val next = current + musicInfo
        _state.update { it.copy(queue = next) }
        player.addMediaItem(musicInfo.toMediaItem())
        ensureResolved(next.lastIndex)
    }

    /**
     * 「稍后播放」：插入到**当前曲目之后**（对应原版列表菜单的 playLater / addTempPlayList）。
     *
     * 与 [addToQueue] 的区别只在插入位置：addToQueue 追加到队尾，本方法插到当前位置之后。
     * 队列为空时退化为「立即入队但不自动播放」，避免出现「点了稍后播放却开始播放」。
     * 队列索引完全交给 ExoPlayer 的 mediaItemIndex 维护（与 next()/playAt() 同一套），
     * 不手改 queueIndex，因此不会与 onMediaItemTransition 的既有状态同步逻辑打架。
     */
    fun playNext(musicInfo: MusicInfo) {
        val current = _state.value.queue
        if (current.isEmpty()) {
            // 空队列：入队并解析 URL，但不抢播放（autoPlay = false）
            _state.update { it.copy(queue = current + musicInfo) }
            player.addMediaItem(musicInfo.toMediaItem())
            ensureResolved(0)
            return
        }
        // 与 addToQueue 一致：id 是 LazyColumn 稳定 key，重复会导致 "Duplicate key" 崩溃
        if (current.any { it.id == musicInfo.id }) return
        val insertAt = player.currentMediaItemIndex.coerceAtLeast(0) + 1
        val next = current.toMutableList().also { it.add(insertAt.coerceAtMost(it.size), musicInfo) }
        _state.update { it.copy(queue = next) }
        // Media3 没有 insertMediaItem：addMediaItem(index, item) 语义就是「插入到 index 位置」，
        // 不会打断正在播放的音频（与重建整个播放列表相比），正是「下一首播放」需要的行为
        runCatching { player.addMediaItem(insertAt, musicInfo.toMediaItem()) }
            .onFailure { // 插入失败（媒体项被并发修改）时，退回追加到队尾，保证「稍后播放」不丢歌
                runCatching { player.addMediaItem(musicInfo.toMediaItem()) }
            }
        // 解析在后台进行；只有轮到它时才会用到 URL，不打断当前播放
        ensureResolved(insertAt)
    }

    fun togglePlay() {        if (player.isPlaying) player.pause() else player.play()
        saveLastPlay()
    }

    /** 下一首：先轮到队尾，再从头部重新开始。返回本次是否回绕（界面据此提示「已回到列表开头」） */
    fun next(): Boolean {
        if (_state.value.queue.isEmpty()) return false
        // 手动切歌在队尾/队首要回绕（原版 playNext/playPrev 会把 single/list 重映射成 listLoop）
        val wrapped: Boolean
        if (player.shuffleModeEnabled) {
            wrapped = !player.hasNextMediaItem()
            if (!wrapped) player.seekToNextMediaItem() else player.seekTo(0, 0L)
        } else {
            val last = _state.value.queue.lastIndex
            wrapped = player.currentMediaItemIndex >= last
            val target = if (!wrapped) player.currentMediaItemIndex + 1 else 0
            player.seekTo(target, 0L)
        }
        // 不在 seekTo 后立即 saveLastPlay：此刻 state.current 还是切歌前的旧曲目。
        // onMediaItemTransition 会在 state 更新后统一保存正确曲目。
        return wrapped
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
        // 同上：由 onMediaItemTransition 在 state 更新后统一保存，避免写入旧曲目
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
    }

    fun clear() {
        playGeneration++
        playQueueJob?.cancel()
        lastPlayJob?.cancel()
        errorNextJob?.cancel()
        player.stop()
        player.clearMediaItems()
        lyricJob?.cancel()
        coverJob?.cancel()
        resolvingIndexes.clear()
        _state.update {
            PlayState(playMode = it.playMode)
        }
    }

    fun release() {
        playGeneration++
        playQueueJob?.cancel()
        lastPlayJob?.cancel()
        lyricJob?.cancel()
        coverJob?.cancel()
        errorNextJob?.cancel()
        timeoutJob?.cancel()
        resolvingIndexes.clear()
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
        // 先清空上一首的封面，避免切到无封面歌曲时界面残留旧图；
        // 加载期间显示占位，取到结果后再填回。
        _state.update { it.copy(coverUrl = null) }
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
                    // 原版 player.isShowNotificationImage：关闭时通知/锁屏不显示专辑封面。
                    // 仅在开关开启时才设置 artworkUri；封面加载与内存缓存不受影响（播放页照常显示）。
                    .setArtworkUri(
                        if (showNotificationImage) meta.picUrl?.let { android.net.Uri.parse(it) } else null
                    )
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
