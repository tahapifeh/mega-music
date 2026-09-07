package com.example.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility responsible for querying Android's MediaStore for local audio files.
 * Safely extracts song metadata including title, artist, album, duration, and album artwork ID.
 */
object MediaStoreScanner {

    suspend fun scanSongs(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Filter out ringtones, notification tones and short sounds (< 10 seconds)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val sizeColumn = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedColumn = it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = if (idColumn != -1) it.getLong(idColumn) else continue
                    val rawTitle = if (titleColumn != -1) it.getString(titleColumn) ?: "Unknown Title" else "Unknown Title"
                    val rawArtist = if (artistColumn != -1) it.getString(artistColumn) ?: "Unknown Artist" else "Unknown Artist"
                    val rawAlbum = if (albumColumn != -1) it.getString(albumColumn) ?: "Unknown Album" else "Unknown Album"
                    val duration = if (durationColumn != -1) it.getLong(durationColumn) else 0L
                    val albumId = if (albumIdColumn != -1) it.getLong(albumIdColumn) else 0L
                    val filePath = if (dataColumn != -1) it.getString(dataColumn) ?: "" else ""
                    val size = if (sizeColumn != -1) it.getLong(sizeColumn) else 0L
                    val dateAdded = if (dateAddedColumn != -1) it.getLong(dateAddedColumn) else 0L

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val cleanArtist = if (rawArtist.trim().equals("<unknown>", ignoreCase = true) || rawArtist.isBlank()) {
                        "Unknown Artist"
                    } else rawArtist.trim()

                    val cleanAlbum = if (rawAlbum.trim().equals("<unknown>", ignoreCase = true) || rawAlbum.isBlank()) {
                        "Unknown Album"
                    } else rawAlbum.trim()

                    songList.add(
                        Song(
                            id = id,
                            title = rawTitle.trim(),
                            artist = cleanArtist,
                            album = cleanAlbum,
                            durationMs = duration,
                            contentUriString = contentUri.toString(),
                            albumId = albumId,
                            folderPath = filePath,
                            sizeBytes = size,
                            dateAdded = dateAdded,
                            isFavorite = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }
}
