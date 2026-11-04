package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val LocalIdeaPalette = staticCompositionLocalOf { IdeaThemes.all[0] }

@Composable
fun IdeaSpaceTheme(
    palette: IdeaPalette = IdeaThemes.all[0],
    content: @Composable () -> Unit
) {
    val scheme = darkColorScheme(
        primary = palette.primary,
        onPrimary = Color(0xFF10121A),
        primaryContainer = palette.primary.copy(alpha = 0.2f),
        onPrimaryContainer = palette.primary,
        secondary = palette.secondary,
        onSecondary = Color.White,
        tertiary = IdeaMint,
        background = palette.sky.last(),
        onBackground = TextPrimaryDark,
        surface = palette.surface,
        onSurface = TextPrimaryDark,
        surfaceVariant = lerp(palette.surface, Color.White, 0.10f),
        onSurfaceVariant = palette.onSurfaceVariant,
        outline = lerp(palette.surface, Color.White, 0.28f)
    )
    CompositionLocalProvider(LocalIdeaPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
    }
}
