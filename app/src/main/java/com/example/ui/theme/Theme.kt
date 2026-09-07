package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Immersive UI Dark Theme Palette (Default)
 * Ultra-dark obsidian background with cosmic violet, deep blue, and electric cyan accents.
 */
private val ImmersiveDarkColorScheme =
  darkColorScheme(
    primary = ImmersiveElectricBlue,
    onPrimary = Color.Black,
    primaryContainer = ImmersiveNavy,
    onPrimaryContainer = ImmersiveElectricBlue,
    secondary = ImmersiveIndigoAccent,
    onSecondary = Color.Black,
    secondaryContainer = ImmersiveSurfaceElevated,
    onSecondaryContainer = ImmersiveIndigoAccent,
    tertiary = ImmersivePurpleAccent,
    onTertiary = Color.White,
    background = ImmersiveBackground,
    onBackground = TextPrimary,
    surface = ImmersiveSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassWhite5,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassWhite10,
  )

/**
 * Clean Light Theme Palette
 */
private val ImmersiveLightColorScheme =
  lightColorScheme(
    primary = ImmersiveBlue600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = ImmersiveIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = ImmersiveDeepPurple,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Immersive UI defaults to dark aesthetic
  dynamicColor: Boolean = false, // Keep immersive aesthetic consistent by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> ImmersiveDarkColorScheme
      else -> ImmersiveLightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

/**
 * Renders the Immersive UI ambient gradient background.
 * Diagonal glow: Deep Purple (#4A148C) -> Deep Blue (#0D47A1) -> Obsidian (#050505)
 */
@Composable
fun ImmersiveAmbientBackdrop(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(ImmersiveBackground)
  ) {
    // Ambient cosmic glow layer
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              ImmersiveDeepPurple.copy(alpha = 0.35f),
              ImmersiveDeepBlue.copy(alpha = 0.25f),
              Color.Transparent,
              ImmersiveBackground
            )
          )
        )
    )
    content()
  }
}
