package com.example.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.MainActivity
import com.example.data.model.PlaybackStatus
import com.example.data.model.PlayerSnapshot
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.service.MusicPlaybackService
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

/**
 * Concrete implementation of MusicPlayer using AndroidX Media3 ExoPlayer and MediaSession.
 */
class Media3MusicPlayer(private val context: Context) : MusicPlayer {

    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressTickerJob: Job? = null

    private val _state = MutableStateFlow(PlayerSnapshot())
    override val state: StateFlow<PlayerSnapshot> = _state.asStateFlow()

    private var currentQueue: List<Song> = emptyList()

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true // Handle audio focus
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private val mediaSession: MediaSession

    init {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(context, exoPlayer)
            .setSessionActivity(pendingIntent)
            .build()

        // Share active session with the background media service without prematurely starting foreground
        MusicPlaybackService.activeSession = mediaSession

        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update {
                    it.copy(
                        isPlaying = isPlaying,
                        playbackStatus = if (isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                    )
                }
                if (isPlaying) {
                    startProgressTicker()
                } else {
                    stopProgressTicker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val status = when (playbackState) {
                    Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
                    Player.STATE_READY -> if (exoPlayer.isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                    Player.STATE_ENDED -> PlaybackStatus.ENDED
                    Player.STATE_IDLE -> PlaybackStatus.IDLE
                    else -> PlaybackStatus.IDLE
                }

                _state.update {
                    it.copy(
                        playbackStatus = status,
                        durationMs = exoPlayer.duration.coerceAtLeast(0L),
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                    )
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentMediaId = mediaItem?.mediaId?.toLongOrNull()
                val activeSong = currentQueue.firstOrNull { it.id == currentMediaId }
                    ?: if (exoPlayer.currentMediaItemIndex in currentQueue.indices) {
                        currentQueue[exoPlayer.currentMediaItemIndex]
                    } else null

                _state.update {
                    it.copy(
                        currentSong = activeSong,
                        queueIndex = exoPlayer.currentMediaItemIndex,
                        durationMs = exoPlayer.duration.coerceAtLeast(activeSong?.durationMs ?: 0L),
                        currentPositionMs = 0L
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        playbackStatus = PlaybackStatus.ERROR,
                        errorMessage = error.localizedMessage ?: "Playback error"
                    )
                }
            }
        })
    }

    override fun playSong(song: Song, queue: List<Song>) {
        currentQueue = queue.ifEmpty { listOf(song) }
        val targetIndex = currentQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItems = currentQueue.map { item ->
            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(item.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .setAlbumTitle(item.album)
                        .setArtworkUri(item.artworkUri)
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, targetIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        _state.update {
            it.copy(
                currentSong = song,
                queue = currentQueue,
                queueIndex = targetIndex,
                isPlaying = true,
                durationMs = song.durationMs,
                errorMessage = null
            )
        }

        startProgressTicker()
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (currentQueue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    override fun previous() {
        if (exoPlayer.currentPosition > 3000 || !exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekTo(0L)
        } else {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun toggleShuffle() {
        val nextShuffleState = !exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = nextShuffleState
        _state.update { it.copy(isShuffleEnabled = nextShuffleState) }
    }

    override fun cycleRepeatMode() {
        val nextMode = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                RepeatMode.ALL
            }
            Player.REPEAT_MODE_ALL -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                RepeatMode.ONE
            }
            else -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                RepeatMode.OFF
            }
        }
        _state.update { it.copy(repeatMode = nextMode) }
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = playerScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val duration = if (exoPlayer.duration > 0) exoPlayer.duration else _state.value.durationMs
                    _state.update {
                        it.copy(
                            currentPositionMs = currentPos,
                            durationMs = duration
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    override fun release() {
        stopProgressTicker()
        if (MusicPlaybackService.activeSession == mediaSession) {
            MusicPlaybackService.activeSession = null
        }
        mediaSession.release()
        exoPlayer.release()
    }
}
