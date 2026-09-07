package com.example.player

import com.example.data.model.PlayerSnapshot
import com.example.data.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * High-level contract for offline music playback.
 * Decouples the UI and ViewModels from Android Media3 implementation details.
 */
interface MusicPlayer {
    /**
     * Observable state stream of the player engine.
     */
    val state: StateFlow<PlayerSnapshot>

    /**
     * Start playback of a specific song within a queue.
     */
    fun playSong(song: Song, queue: List<Song> = listOf(song))

    /**
     * Resume or start playback.
     */
    fun play()

    /**
     * Pause playback.
     */
    fun pause()

    /**
     * Skip to the next track in the queue.
     */
    fun next()

    /**
     * Skip to previous track or seek to beginning if past 3 seconds.
     */
    fun previous()

    /**
     * Seek to a position in milliseconds within the current track.
     */
    fun seekTo(positionMs: Long)

    /**
     * Toggle shuffle mode on / off.
     */
    fun toggleShuffle()

    /**
     * Cycle through OFF -> ALL -> ONE repeat modes.
     */
    fun cycleRepeatMode()

    /**
     * Release player resources when shutting down.
     */
    fun release()
}
