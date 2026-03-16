package com.humans.aura.core.services.preferences

import android.content.Context
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.AppPreferencesRepository
import com.humans.aura.core.domain.interfaces.ThemePreferenceRepository
import com.humans.aura.core.domain.models.AppPreferencesSnapshot
import com.humans.aura.core.domain.models.AppThemeModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesAppLaunchRepository(
    context: Context,
) : AppLaunchRepository, AppPreferencesRepository, ThemePreferenceRepository {

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val themeModePreferenceState = MutableStateFlow(readThemeModePreference())

    override suspend fun hasCompletedInitialStopwatchBootstrap(): Boolean =
        sharedPreferences.getBoolean(KEY_INITIAL_STOPWATCH_BOOTSTRAPPED, false)

    override suspend fun markInitialStopwatchBootstrapCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_INITIAL_STOPWATCH_BOOTSTRAPPED, true)
            .apply()
    }

    override suspend fun snapshot(): AppPreferencesSnapshot = AppPreferencesSnapshot(
        hasCompletedInitialStopwatchBootstrap = hasCompletedInitialStopwatchBootstrap(),
        themeModePreference = themeModePreferenceState.value,
    )

    override suspend fun restore(snapshot: AppPreferencesSnapshot) {
        sharedPreferences.edit()
            .putBoolean(
                KEY_INITIAL_STOPWATCH_BOOTSTRAPPED,
                snapshot.hasCompletedInitialStopwatchBootstrap,
            )
            .putString(KEY_THEME_MODE_PREFERENCE, snapshot.themeModePreference.name)
            .apply()
        themeModePreferenceState.value = snapshot.themeModePreference
    }

    override fun observeThemeModePreference(): Flow<AppThemeModePreference> = themeModePreferenceState.asStateFlow()

    override suspend fun setThemeModePreference(themeModePreference: AppThemeModePreference) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE_PREFERENCE, themeModePreference.name)
            .apply()
        themeModePreferenceState.value = themeModePreference
    }

    private fun readThemeModePreference(): AppThemeModePreference =
        sharedPreferences.getString(KEY_THEME_MODE_PREFERENCE, AppThemeModePreference.DEVICE.name)
            .let(::parseThemeModePreference)
            ?: AppThemeModePreference.DEVICE

    private companion object {
        private const val PREFERENCES_NAME = "aura_launch_state"
        private const val KEY_INITIAL_STOPWATCH_BOOTSTRAPPED = "initial_stopwatch_bootstrapped"
        private const val KEY_THEME_MODE_PREFERENCE = "theme_mode_preference"
    }
}

private fun parseThemeModePreference(rawValue: String?): AppThemeModePreference =
    AppThemeModePreference.values().firstOrNull { it.name == rawValue } ?: AppThemeModePreference.DEVICE
