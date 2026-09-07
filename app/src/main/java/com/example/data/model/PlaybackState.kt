package com.example.data.model

/**
 * Modes for playlist repetition.
 */
enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

/**
 * State of playback engine.
 */
enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}

/**
 * Snapshot of the current playback engine state.
 * Emitted by Player and observed reactively by ViewModels and Compose UI.
 */
data class PlayerSnapshot(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackStatus: PlaybackStatus = PlaybackStatus.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}
