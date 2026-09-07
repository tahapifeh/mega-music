package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.EmptyState
import com.example.ui.components.SongItem
import com.example.ui.components.VibeSearchBar
import com.example.ui.theme.GlassWhite5
import com.example.ui.theme.ImmersiveElectricBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MusicUiState

@Composable
fun HomeScreen(
    uiState: MusicUiState,
    currentSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToSongs: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!uiState.hasPermission) {
        EmptyState(
            title = "Audio Permission Required",
            description = "Vibe Music needs permission to scan and play music stored on your device.",
            actionButtonText = "Grant Permission",
            onActionClick = onNavigateToSongs,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // App Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Vibe Music",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Your offline library",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar shortcut
                BoxClickable(onClick = onSearchClick) {
                    VibeSearchBar(
                        query = "",
                        onQueryChange = { onSearchClick() },
                        placeholder = "Search songs, artists, albums..."
                    )
                }
            }
        }

        // Recently Played Carousel (if any)
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(uiState.recentlyPlayed.take(8), key = { "recent_${it.id}" }) { song ->
                        RecentSongCard(
                            song = song,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }

        // Favorites Preview (if any)
        if (uiState.favorites.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Favorites",
                    actionText = "See All (${uiState.favorites.size})",
                    onAction = onNavigateToSongs
                )
            }

            items(uiState.favorites.take(3), key = { "fav_preview_${it.id}" }) { song ->
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

        // Recently Added / Library Tracks
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Recently Added",
                actionText = "View All (${uiState.songs.size})",
                onAction = onNavigateToSongs
            )
        }

        if (uiState.songs.isEmpty()) {
            item {
                EmptyState(
                    title = "No Music Found",
                    description = "We couldn't detect any audio files on your device. Rescan or transfer songs to begin.",
                    actionButtonText = "Rescan Library",
                    onActionClick = onNavigateToSongs,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            items(uiState.songs.take(15), key = { "home_song_${it.id}" }) { song ->
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

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
        )

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ImmersiveElectricBlue,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun RecentSongCard(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite5),
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AlbumArtwork(
                song = song,
                size = 110.dp,
                shapeRadius = 12.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BoxClickable(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        content()
    }
}
