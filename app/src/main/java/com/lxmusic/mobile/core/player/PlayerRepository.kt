package com.lxmusic.mobile.core.player

import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.lxmusic.mobile.core.model.MusicInfo
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

/** 歌词行 */
data class LyricLine(val timeMs: Long, val text: String)

/** 播放状态 */
data class PlayState(
    val current: MusicInfo? = null,
    val queue: List<MusicInfo> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: String = PlayMode.LIST_LOOP,
    val lyricLines: List<LyricLine> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
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
            true,
        )
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var lyricJob: Job? = null

    /** 歌词加载器（由 App 注入，避免 core 层反向依赖） */
    var lyricLoader: (suspend (MusicInfo) -> String?)? = null

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
                val index = player.currentMediaItemIndex
                val current = _state.value.queue.getOrNull(index)
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

    /** 播放指定队列 */
    fun playQueue(queue: List<MusicInfo>, index: Int) {
        if (queue.isEmpty()) return
        val safeIndex = index.coerceIn(0, queue.lastIndex)
        _state.update {
            it.copy(
                queue = queue,
                queueIndex = safeIndex,
                current = queue[safeIndex],
                lyricLines = emptyList(),
                positionMs = 0L,
                durationMs = 0L,
                error = null,
            )
        }
        player.setMediaItems(queue.map { it.toMediaItem() }, safeIndex, 0L)
        player.prepare()
        player.play()
        loadLyric(queue[safeIndex])
    }

    /** 播放单曲（替换整个队列） */
    fun playSingle(musicInfo: MusicInfo) = playQueue(listOf(musicInfo), 0)

    /** 追加到队列 */
    fun addToQueue(musicInfo: MusicInfo) {
        _state.update { it.copy(queue = it.queue + musicInfo) }
        player.addMediaItem(musicInfo.toMediaItem())
    }

    fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun prev() {
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun setPlayMode(mode: String) {
        _state.update { it.copy(playMode = mode) }
        when (mode) {
            PlayMode.SINGLE_LOOP -> player.repeatMode = Player.REPEAT_MODE_ONE
            PlayMode.LIST_LOOP, PlayMode.RANDOM -> player.repeatMode = Player.REPEAT_MODE_ALL
            else -> player.repeatMode = Player.REPEAT_MODE_OFF
        }
        player.shuffleModeEnabled = mode == PlayMode.RANDOM
    }

    fun clear() {
        player.stop()
        player.clearMediaItems()
        lyricJob?.cancel()
        _state.update {
            PlayState(playMode = it.playMode)
        }
    }

    fun release() {
        lyricJob?.cancel()
        player.release()
    }

    /** 加载当前歌曲歌词 */
    private fun loadLyric(musicInfo: MusicInfo) {
        val loader = lyricLoader ?: return
        lyricJob?.cancel()
        lyricJob = scope.launch {
            val lyric = runCatching { loader(musicInfo) }.getOrNull().orEmpty()
            // 若期间已切歌则丢弃结果
            if (_state.value.current?.id != musicInfo.id) return@launch
            _state.update { it.copy(lyricLines = LrcParser.parse(lyric)) }
        }
    }

    private fun MusicInfo.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(meta.filePath ?: "")
            .setMediaId(id)
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
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
