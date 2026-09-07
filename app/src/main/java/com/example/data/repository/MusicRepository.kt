package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FavoriteEntity
import com.example.data.model.Song
import com.example.utils.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository coordinating between device MediaStore audio files and local Room persistence.
 */
class MusicRepository(private val context: Context) {

    private val favoriteDao = AppDatabase.getInstance(context).favoriteDao()

    /**
     * Scans songs from MediaStore and maps them with reactive favorite flags from Room.
     */
    fun getSongsWithFavorites(): Flow<List<Song>> = flow {
        val scannedSongs = MediaStoreScanner.scanSongs(context)
        emit(scannedSongs)
    }.combine(favoriteDao.getAllFavoriteIds()) { songs, favoriteIds ->
        val favoriteSet = favoriteIds.toSet()
        songs.map { song ->
            song.copy(isFavorite = favoriteSet.contains(song.id))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Retrieves all favorites as a live stream of Song models.
     */
    fun getFavorites(): Flow<List<Song>> = combine(
        flow { emit(MediaStoreScanner.scanSongs(context)) },
        favoriteDao.getAllFavorites()
    ) { scannedSongs, favoriteEntities ->
        val scannedMap = scannedSongs.associateBy { it.id }
        favoriteEntities.mapNotNull { entity ->
            scannedMap[entity.songId]?.copy(isFavorite = true) ?: Song(
                id = entity.songId,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                durationMs = entity.durationMs,
                contentUriString = entity.contentUriString,
                albumId = entity.albumId,
                isFavorite = true
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Toggles the favorite status of a song in Room.
     */
    suspend fun toggleFavorite(song: Song) = withContext(Dispatchers.IO) {
        if (song.isFavorite) {
            favoriteDao.deleteFavoriteBySongId(song.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    durationMs = song.durationMs,
                    contentUriString = song.contentUriString,
                    albumId = song.albumId
                )
            )
        }
    }
}
