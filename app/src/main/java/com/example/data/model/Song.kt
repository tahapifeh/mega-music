package com.example.data.model

import android.net.Uri

/**
 * Immutable data model representing a local music track.
 * Extracted from Android MediaStore with support for offline playback,
 * favorites tracking, and playlist association.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUriString: String,
    val albumId: Long = 0L,
    val folderPath: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val isFavorite: Boolean = false
) {
    /**
     * Helper to resolve the content URI for Media3 or ExoPlayer.
     */
    val contentUri: Uri
        get() = Uri.parse(contentUriString)

    /**
     * Helper to resolve standard MediaStore album artwork URI.
     */
    val artworkUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")

    /**
     * Formats duration in mm:ss or hh:mm:ss format.
     */
    fun formatDuration(): String {
        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
