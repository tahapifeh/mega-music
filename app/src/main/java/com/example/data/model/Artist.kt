package com.example.data.model

/**
 * Model representing an Artist grouped from songs.
 */
data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int = 1,
    val songs: List<Song> = emptyList()
)
