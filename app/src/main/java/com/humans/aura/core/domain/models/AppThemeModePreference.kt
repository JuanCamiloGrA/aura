package com.humans.aura.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class AppThemeModePreference {
    DEVICE,
    LIGHT,
    DARK,
}
