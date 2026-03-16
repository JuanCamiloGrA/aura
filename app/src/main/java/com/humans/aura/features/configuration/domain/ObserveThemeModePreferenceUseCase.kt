package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.ThemePreferenceRepository

class ObserveThemeModePreferenceUseCase(
    private val themePreferenceRepository: ThemePreferenceRepository,
) {
    operator fun invoke() = themePreferenceRepository.observeThemeModePreference()
}
