package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity

class EnsureInitialActivityUseCase(
    private val activityRepository: ActivityRepository,
    private val appLaunchRepository: AppLaunchRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): Activity? {
        if (appLaunchRepository.hasCompletedInitialStopwatchBootstrap()) {
            return null
        }

        if (activityRepository.hasLoggedActivities()) {
            appLaunchRepository.markInitialStopwatchBootstrapCompleted()
            return null
        }

        val initialActivity = activityRepository.logNewActivity(
            LogNewActivityCommand(
                title = DEFAULT_INITIAL_ACTIVITY_TITLE,
                timestampEpochMillis = timeProvider.currentTimeMillis(),
            ),
        )

        appLaunchRepository.markInitialStopwatchBootstrapCompleted()
        return initialActivity
    }

    companion object {
        const val DEFAULT_INITIAL_ACTIVITY_TITLE = "Open AURA and choose today's focus"
    }
}
