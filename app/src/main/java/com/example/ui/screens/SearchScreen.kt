package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.EmptyState
import com.example.ui.components.SongItem
import com.example.ui.components.VibeSearchBar
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MusicUiState

@Composable
fun SearchScreen(
    uiState: MusicUiState,
    currentSongId: Long?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            VibeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onQueryChange,
                placeholder = "Search songs, artists, albums..."
            )
        }

        if (uiState.searchQuery.isBlank()) {
            EmptyState(
                title = "Search your music",
                description = "Type a song title, artist, or album name to search across your local offline library.",
                icon = Icons.Default.SearchOff,
                modifier = Modifier.weight(1f)
            )
        } else if (uiState.searchResults.isEmpty()) {
            EmptyState(
                title = "No results found",
                description = "No songs matched \"${uiState.searchQuery}\". Try checking for typos or searching by artist.",
                icon = Icons.Default.SearchOff,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "Found ${uiState.searchResults.size} matches",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(uiState.searchResults, key = { "search_${it.id}" }) { song ->
                    SongItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = song.id == currentSongId,
                        onSongClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
