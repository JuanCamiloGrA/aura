package com.humans.aura.core.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.humans.aura.core.domain.models.AppThemeModePreference

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
private val OledDarkColors = darkColorScheme(
    primary = AuraWhite,
    onPrimary = AuraBlack,
    primaryContainer = AuraDarkestSurface,
    onPrimaryContainer = AuraWhite,
    secondary = AuraMist,
    onSecondary = AuraBlack,
    secondaryContainer = AuraDarkestSurface,
    onSecondaryContainer = AuraMist,
    background = AuraBlack,
    onBackground = AuraWhite,
    surface = AuraBlack,
    onSurface = AuraWhite,
    surfaceVariant = AuraDarkestSurface,
    onSurfaceVariant = AuraLight,
    surfaceContainerLowest = AuraBlack,
    surfaceContainerLow = AuraDarkestSurface,
    surfaceContainer = AuraDarkestSurface,
    surfaceContainerHigh = AuraInk,
    surfaceContainerHighest = AuraDark,
    outline = AuraDark,
    outlineVariant = AuraDarkestSurface,
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
 * @param themeModePreference Controls whether the app follows the device,
 * uses light mode, or forces OLED dark mode.
 */
@Composable
fun AuraTheme(
    themeModePreference: AppThemeModePreference = AppThemeModePreference.DEVICE,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeModePreference) {
        AppThemeModePreference.DEVICE -> isSystemInDarkTheme()
        AppThemeModePreference.LIGHT -> false
        AppThemeModePreference.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) OledDarkColors else LightColors,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content,
    )
}
