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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 播放模式（原版 player.togglePlayMethod） */
object PlayMode {
    const val LIST_LOOP = "listLoop"
    const val RANDOM = "random"
    const val LIST = "list"
    const val SINGLE_LOOP = "singleLoop"
    const val SINGLE = "single"
}

/** 播放状态 */
data class PlayState(
    val current: MusicInfo? = null,
    val queue: List<MusicInfo> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: String = PlayMode.LIST_LOOP,
    val lyric: String = "",
    val loading: Boolean = false,
)

/**
 * 播放仓库：Media3 ExoPlayer + 播放队列管理。
 * 对应原版 react-native-track-player + core/player/player.ts 的职责。
 */
class PlayerRepository(context: Context) {

    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        .build()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(loading = playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_ENDED) {
                    handleEnded()
                }
            }
        })
    }

    private val positionUpdater = Thread {
        while (true) {
            runCatching {
                if (player.isPlaying) {
                    _state.value = _state.value.copy(
                        positionMs = player.currentPosition,
                        durationMs = player.duration,
                    )
                }
            }
            Thread.sleep(500)
        }
    }.apply { isDaemon = true; start() }

    /** 播放指定队列 */
    fun playQueue(queue: List<MusicInfo>, index: Int) {
        if (queue.isEmpty()) return
        val safeIndex = index.coerceIn(0, queue.lastIndex)
        _state.value = _state.value.copy(queue = queue, queueIndex = safeIndex)
        player.setMediaItems(queue.map { it.toMediaItem() }, safeIndex, 0L)
        player.prepare()
        player.play()
    }

    /** 播放单曲（替换整个队列） */
    fun playSingle(musicInfo: MusicInfo) = playQueue(listOf(musicInfo), 0)

    /** 追加到队列 */
    fun addToQueue(musicInfo: MusicInfo) {
        val newQueue = _state.value.queue + musicInfo
        _state.value = _state.value.copy(queue = newQueue)
        player.addMediaItem(musicInfo.toMediaItem())
    }

    fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            _state.value = _state.value.copy(queueIndex = _state.value.queueIndex + 1)
        }
    }

    fun prev() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            _state.value = _state.value.copy(queueIndex = _state.value.queueIndex - 1)
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun setPlayMode(mode: String) {
        _state.value = _state.value.copy(playMode = mode)
        when (mode) {
            PlayMode.SINGLE_LOOP -> player.repeatMode = Player.REPEAT_MODE_ONE
            PlayMode.LIST_LOOP -> player.repeatMode = Player.REPEAT_MODE_ALL
            else -> player.repeatMode = Player.REPEAT_MODE_OFF
        }
        player.shuffleModeEnabled = mode == PlayMode.RANDOM
    }

    fun clear() {
        player.stop()
        player.clearMediaItems()
        _state.value = PlayState(playMode = _state.value.playMode)
    }

    private fun handleEnded() {
        when (_state.value.playMode) {
            PlayMode.SINGLE, PlayMode.LIST -> {
                if (!player.hasNextMediaItem()) {
                    player.pause()
                    player.seekTo(0)
                }
            }
            else -> Unit // loop 模式由 repeatMode 处理
        }
    }

    fun release() {
        player.release()
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
