package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AppThemeModePreference
import kotlinx.coroutines.flow.Flow

interface ThemePreferenceRepository {
    fun observeThemeModePreference(): Flow<AppThemeModePreference>

    suspend fun setThemeModePreference(themeModePreference: AppThemeModePreference)
}
