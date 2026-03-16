package com.humans.aura.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferencesSnapshot(
    val hasCompletedInitialStopwatchBootstrap: Boolean,
    val themeModePreference: AppThemeModePreference = AppThemeModePreference.DEVICE,
)
