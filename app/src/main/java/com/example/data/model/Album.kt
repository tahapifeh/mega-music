package com.example.data.model

import android.net.Uri

/**
 * Model representing an Album grouped from songs.
 */
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
) {
    val artworkUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$id")
}
