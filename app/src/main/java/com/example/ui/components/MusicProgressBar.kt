package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.ImmersiveElectricBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TimeStampStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isUserDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayProgress = if (isUserDragging) dragProgress else actualProgress

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = displayProgress,
            onValueChange = { newValue ->
                isUserDragging = true
                dragProgress = newValue
            },
            onValueChangeFinished = {
                val targetMs = (dragProgress * durationMs).toLong()
                onSeek(targetMs)
                isUserDragging = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            colors = SliderDefaults.colors(
                thumbColor = ImmersiveElectricBlue,
                activeTrackColor = ImmersiveElectricBlue,
                inactiveTrackColor = GlassHighlight
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayedPosMs = if (isUserDragging) (dragProgress * durationMs).toLong() else currentPositionMs
            Text(
                text = formatTime(displayedPosMs),
                style = TimeStampStyle.copy(color = TextDim)
            )
            Text(
                text = formatTime(durationMs),
                style = TimeStampStyle.copy(color = TextDim)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
