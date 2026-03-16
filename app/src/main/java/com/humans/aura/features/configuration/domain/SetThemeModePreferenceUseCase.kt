package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.ThemePreferenceRepository
import com.humans.aura.core.domain.models.AppThemeModePreference

class SetThemeModePreferenceUseCase(
    private val themePreferenceRepository: ThemePreferenceRepository,
) {
    suspend operator fun invoke(themeModePreference: AppThemeModePreference) {
        themePreferenceRepository.setThemeModePreference(themeModePreference)
    }
}
