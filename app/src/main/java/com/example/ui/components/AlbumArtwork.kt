package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Song
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImmersiveDeepBlue
import com.example.ui.theme.ImmersiveDeepPurple
import com.example.ui.theme.ImmersiveElectricBlue

@Composable
fun AlbumArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    shapeRadius: Dp = 12.dp,
    elevation: Dp = 4.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(shapeRadius)

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, shape)
            .clip(shape)
            .border(1.dp, GlassBorder, shape)
            .background(
                brush = Brush.linearGradient(
                    listOf(ImmersiveDeepPurple, ImmersiveDeepBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (song != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "${song.title} artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = null,
                fallback = null
            )
        }

        // Fallback music icon if no image or still loading
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = ImmersiveElectricBlue.copy(alpha = 0.8f),
            modifier = Modifier.size(size * 0.45f)
        )
    }
}
