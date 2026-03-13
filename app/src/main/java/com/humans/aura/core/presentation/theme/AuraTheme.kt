package com.humans.aura.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AuraOlive,
    onPrimary = AuraSurface,
    secondary = AuraClay,
    onSecondary = AuraSurface,
    secondaryContainer = AuraMist,
    onSecondaryContainer = AuraInk,
    background = AuraCanvas,
    onBackground = AuraInk,
    surface = AuraSurface,
    onSurface = AuraInk,
    surfaceVariant = AuraMist,
    onSurfaceVariant = AuraInk,
)

private val DarkColors = darkColorScheme(
    primary = AuraMist,
    onPrimary = AuraInk,
    secondary = AuraClay,
    onSecondary = AuraInk,
    secondaryContainer = AuraOlive,
    onSecondaryContainer = AuraSurface,
    background = AuraInk,
    onBackground = AuraCanvas,
    surface = AuraInk,
    onSurface = AuraCanvas,
    surfaceVariant = AuraOlive,
    onSurfaceVariant = AuraCanvas,
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
