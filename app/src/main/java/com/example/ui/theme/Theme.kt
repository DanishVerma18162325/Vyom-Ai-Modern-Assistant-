package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VyomVioletGlow,
    secondary = VyomCelestialBlue,
    tertiary = VyomCosmicIndigo,
    background = VyomDarkBackground,
    surface = VyomDarkSurface,
    surfaceVariant = VyomDarkBorder,
    outline = VyomDarkBorder,
    onPrimary = VyomWhite,
    onBackground = VyomDarkText,
    onSurface = VyomDarkText,
    onSurfaceVariant = VyomDarkTextSec
)

private val LightColorScheme = lightColorScheme(
    primary = VyomCosmicIndigo,
    secondary = VyomCelestialBlue,
    tertiary = VyomVioletGlow,
    background = VyomWhite,
    surface = VyomOffWhite,
    surfaceVariant = VyomSurface,
    surfaceContainer = VyomSurfaceContainer,
    outline = VyomBorder,
    outlineVariant = VyomBorderSubtle,
    onPrimary = VyomWhite,
    onBackground = VyomTextPrimary,
    onSurface = VyomTextPrimary,
    onSurfaceVariant = VyomTextSecondary
)

@Composable
fun VyomTheme(
    darkTheme: Boolean = false, // White-first interface by default as required
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VyomTheme(darkTheme = darkTheme, content = content)
}
