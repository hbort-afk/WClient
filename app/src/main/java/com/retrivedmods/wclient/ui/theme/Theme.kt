package com.retrivedmods.wclient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium dark red theme
private val PremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF3D00),       // Vivid red-orange
    onPrimary = Color.Black,
    secondary = Color(0xFFD50000),     // Deep red
    onSecondary = Color.White,
    background = Color(0xFF121212),    // Jet black background
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),       // Rich dark grey for elevation
    onSurface = Color.White,
    error = Color(0xFFCF6679),         // Subtle error red
    onError = Color.Black
)

@Composable
fun MuCuteClientTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        content = content
    )
}
