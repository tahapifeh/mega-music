package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayer
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite5
import com.example.ui.theme.ImmersiveAmbientBackdrop
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBlue400
import com.example.ui.theme.ImmersiveElectricBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MusicViewModel

enum class NavigationDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SONGS("Songs", Icons.Default.MusicNote),
    SEARCH("Search", Icons.Default.Search),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PLAYLISTS("Library", Icons.Default.QueueMusic)
}

@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerSnapshot by viewModel.playerState.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(NavigationDestination.HOME) }

    // Intercept back button to dismiss Now Playing if open, or return to Home screen if on another tab
    BackHandler(enabled = uiState.isNowPlayingVisible || currentDestination != NavigationDestination.HOME) {
        if (uiState.isNowPlayingVisible) {
            viewModel.closeNowPlaying()
        } else if (currentDestination != NavigationDestination.HOME) {
            currentDestination = NavigationDestination.HOME
        }
    }

    ImmersiveAmbientBackdrop {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // Floating Mini Player docked right above bottom navigation
                        MiniPlayer(
                            playerSnapshot = playerSnapshot,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.next() },
                            onOpenNowPlaying = { viewModel.openNowPlaying() }
                        )

                        // Bottom Navigation Bar
                        val navShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        NavigationBar(
                            containerColor = GlassWhite5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(navShape)
                                .border(1.dp, GlassBorder, navShape),
                            tonalElevation = 0.dp
                        ) {
                            NavigationDestination.entries.forEach { destination ->
                                val isSelected = currentDestination == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = destination },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = destination.label,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ImmersiveBlue400,
                                        selectedTextColor = ImmersiveBlue400,
                                        indicatorColor = ImmersiveBlue400.copy(alpha = 0.15f),
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        NavigationDestination.HOME -> HomeScreen(
                            uiState = uiState,
                            currentSongId = playerSnapshot.currentSong?.id,
                            isPlaying = playerSnapshot.isPlaying,
                            onSongClick = { song -> viewModel.playSong(song) },
                            onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                            onSearchClick = { currentDestination = NavigationDestination.SEARCH },
                            onNavigateToSongs = { currentDestination = NavigationDestination.SONGS }
                        )

                        NavigationDestination.SONGS -> SongsScreen(
                            uiState = uiState,
                            currentSongId = playerSnapshot.currentSong?.id,
                            isPlaying = playerSnapshot.isPlaying,
                            onSongClick = { song -> viewModel.playSong(song) },
                            onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                            onRescanClick = { viewModel.loadMusicLibrary() },
                            onShuffleAllClick = {
                                if (uiState.songs.isNotEmpty()) {
                                    val shuffled = uiState.songs.shuffled()
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            },
                            onGrantPermissionClick = onRequestPermission
                        )

                        NavigationDestination.SEARCH -> SearchScreen(
                            uiState = uiState,
                            currentSongId = playerSnapshot.currentSong?.id,
                            isPlaying = playerSnapshot.isPlaying,
                            onQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                            onSongClick = { song -> viewModel.playSong(song) },
                            onFavoriteClick = { song -> viewModel.toggleFavorite(song) }
                        )

                        NavigationDestination.FAVORITES -> FavoritesScreen(
                            uiState = uiState,
                            currentSongId = playerSnapshot.currentSong?.id,
                            isPlaying = playerSnapshot.isPlaying,
                            onSongClick = { song -> viewModel.playSong(song, uiState.favorites) },
                            onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                            onPlayAllFavorites = {
                                if (uiState.favorites.isNotEmpty()) {
                                    viewModel.playSong(uiState.favorites.first(), uiState.favorites)
                                }
                            }
                        )

                        NavigationDestination.PLAYLISTS -> PlaylistsScreen(
                            uiState = uiState,
                            onNavigateToFavorites = { currentDestination = NavigationDestination.FAVORITES },
                            onNavigateToSongs = { currentDestination = NavigationDestination.SONGS },
                            onPlayRecent = {
                                if (uiState.recentlyPlayed.isNotEmpty()) {
                                    viewModel.playSong(uiState.recentlyPlayed.first(), uiState.recentlyPlayed)
                                }
                            }
                        )
                    }
                }
            }

            // Now Playing Full-Screen Modal Overlay
            AnimatedVisibility(
                visible = uiState.isNowPlayingVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    playerSnapshot = playerSnapshot,
                    onCollapse = { viewModel.closeNowPlaying() },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onSeek = { targetMs -> viewModel.seekTo(targetMs) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = {
                        playerSnapshot.currentSong?.let { viewModel.toggleFavorite(it) }
                    }
                )
            }
        }
    }
}
