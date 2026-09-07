package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PlayerSnapshot
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.Media3MusicPlayer
import com.example.player.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true,
    val isNowPlayingVisible: Boolean = false
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    val player: MusicPlayer = Media3MusicPlayer(application)

    val playerState: StateFlow<PlayerSnapshot> = player.state

    private val _uiState = MutableStateFlow(MusicUiState(isLoading = true))
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    init {
        loadMusicLibrary()
        observeFavorites()
    }

    fun setPermissionState(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) {
            loadMusicLibrary()
        } else {
            _uiState.update { it.copy(isLoading = false, songs = emptyList()) }
        }
    }

    fun loadMusicLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getSongsWithFavorites().collect { songs ->
                _uiState.update { state ->
                    val filteredSearch = if (state.searchQuery.isBlank()) {
                        emptyList()
                    } else {
                        filterSongs(songs, state.searchQuery)
                    }
                    state.copy(
                        songs = songs,
                        searchResults = filteredSearch,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                filterSongs(state.songs, query)
            }
            state.copy(searchQuery = query, searchResults = results)
        }
    }

    private fun filterSongs(songs: List<Song>, query: String): List<Song> {
        val q = query.trim().lowercase()
        return songs.filter { song ->
            song.title.lowercase().contains(q) ||
                song.artist.lowercase().contains(q) ||
                song.album.lowercase().contains(q)
        }
    }

    fun playSong(song: Song, customQueue: List<Song>? = null) {
        val queue = customQueue ?: _uiState.value.songs
        player.playSong(song, queue)

        // Add to recently played list without duplicates
        _uiState.update { state ->
            val updatedRecent = listOf(song) + state.recentlyPlayed.filterNot { it.id == song.id }
            state.copy(recentlyPlayed = updatedRecent.take(20))
        }
    }

    fun togglePlayPause() {
        val current = playerState.value
        if (current.isPlaying) {
            player.pause()
        } else {
            if (current.currentSong == null && _uiState.value.songs.isNotEmpty()) {
                playSong(_uiState.value.songs.first())
            } else {
                player.play()
            }
        }
    }

    fun next() {
        player.next()
    }

    fun previous() {
        player.previous()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun toggleShuffle() {
        player.toggleShuffle()
    }

    fun cycleRepeatMode() {
        player.cycleRepeatMode()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun openNowPlaying() {
        _uiState.update { it.copy(isNowPlayingVisible = true) }
    }

    fun closeNowPlaying() {
        _uiState.update { it.copy(isNowPlayingVisible = false) }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
