package com.humans.aura.core.services.preferences

import android.content.Context
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.AppPreferencesRepository
import com.humans.aura.core.domain.models.AppPreferencesSnapshot

class SharedPreferencesAppLaunchRepository(
    context: Context,
) : AppLaunchRepository, AppPreferencesRepository {

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun hasCompletedInitialStopwatchBootstrap(): Boolean =
        sharedPreferences.getBoolean(KEY_INITIAL_STOPWATCH_BOOTSTRAPPED, false)

    override suspend fun markInitialStopwatchBootstrapCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_INITIAL_STOPWATCH_BOOTSTRAPPED, true)
            .apply()
    }

    override suspend fun snapshot(): AppPreferencesSnapshot = AppPreferencesSnapshot(
        hasCompletedInitialStopwatchBootstrap = hasCompletedInitialStopwatchBootstrap(),
    )

    override suspend fun restore(snapshot: AppPreferencesSnapshot) {
        sharedPreferences.edit()
            .putBoolean(
                KEY_INITIAL_STOPWATCH_BOOTSTRAPPED,
                snapshot.hasCompletedInitialStopwatchBootstrap,
            )
            .apply()
    }

    private companion object {
        private const val PREFERENCES_NAME = "aura_launch_state"
        private const val KEY_INITIAL_STOPWATCH_BOOTSTRAPPED = "initial_stopwatch_bootstrapped"
    }
}
