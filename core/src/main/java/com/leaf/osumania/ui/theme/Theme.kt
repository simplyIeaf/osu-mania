package com.leaf.osumania.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OsuDarkColors = darkColorScheme(
    primary = Primary,
    secondary = AccentBlue,
    tertiary = AccentPurple,
    background = Background,
    surface = Panel,
    surfaceVariant = PanelLight,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = AccentRed
)

@Composable
fun OsuManiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OsuDarkColors,
        typography = OsuTypography,
        content = content
    )
}
