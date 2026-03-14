package com.humans.aura.core.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// ── Light (default) ─────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary = AuraBlack,
    onPrimary = AuraWhite,
    primaryContainer = AuraSnow,
    onPrimaryContainer = AuraBlack,
    secondary = AuraDark,
    onSecondary = AuraWhite,
    secondaryContainer = AuraSnow,
    onSecondaryContainer = AuraInk,
    background = AuraWhite,
    onBackground = AuraBlack,
    surface = AuraWhite,
    onSurface = AuraBlack,
    surfaceVariant = AuraSnow,
    onSurfaceVariant = AuraMedium,
    surfaceContainerLowest = AuraWhite,
    surfaceContainerLow = AuraSnow,
    surfaceContainer = AuraSnow,
    surfaceContainerHigh = AuraMist,
    surfaceContainerHighest = AuraMist,
    outline = AuraMist,
    outlineVariant = AuraSnow,
    error = AuraError,
    onError = AuraWhite,
    errorContainer = AuraErrorContainer,
    onErrorContainer = AuraError,
    inverseSurface = AuraInk,
    inverseOnSurface = AuraWhite,
    inversePrimary = AuraWhite,
)

// ── Dark (night / sleep mode) ───────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary = AuraWhite,
    onPrimary = AuraBlack,
    primaryContainer = AuraInk,
    onPrimaryContainer = AuraWhite,
    secondary = AuraLight,
    onSecondary = AuraBlack,
    secondaryContainer = AuraInk,
    onSecondaryContainer = AuraMist,
    background = AuraBlack,
    onBackground = AuraWhite,
    surface = AuraBlack,
    onSurface = AuraWhite,
    surfaceVariant = AuraInk,
    onSurfaceVariant = AuraMedium,
    surfaceContainerLowest = AuraBlack,
    surfaceContainerLow = AuraInk,
    surfaceContainer = AuraInk,
    surfaceContainerHigh = AuraDark,
    surfaceContainerHighest = AuraDark,
    outline = AuraDark,
    outlineVariant = AuraInk,
    error = AuraError,
    onError = AuraWhite,
    errorContainer = AuraError,
    onErrorContainer = AuraWhite,
    inverseSurface = AuraWhite,
    inverseOnSurface = AuraBlack,
    inversePrimary = AuraBlack,
)

// ── Shape scale ─────────────────────────────────────────────────────────────
private val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Aura design system entry point.
 *
 * @param darkTheme Pass `true` for night / sleep-mode dark theme.
 *                  Defaults to `false` (light mode).
 */
@Composable
fun AuraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content,
    )
}
