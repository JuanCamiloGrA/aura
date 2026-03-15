package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnsureInitialActivityUseCaseTest {

    @Test
    fun invoke_logs_default_activity_on_first_launch_without_history() = runTest {
        val activityRepository = FakeActivityRepository(hasLoggedActivities = false)
        val appLaunchRepository = FakeAppLaunchRepository(hasBootstrapped = false)
        val useCase = EnsureInitialActivityUseCase(
            activityRepository = activityRepository,
            appLaunchRepository = appLaunchRepository,
            timeProvider = FakeTimeProvider(now = 42L),
        )

        val result = useCase()

        assertEquals(EnsureInitialActivityUseCase.DEFAULT_INITIAL_ACTIVITY_TITLE, result?.title)
        assertEquals(EnsureInitialActivityUseCase.DEFAULT_INITIAL_ACTIVITY_TITLE, activityRepository.loggedTitles.single())
        assertEquals(1, appLaunchRepository.markCompletedCalls)
    }

    @Test
    fun invoke_logs_default_activity_when_history_is_empty_even_if_bootstrap_already_completed() = runTest {
        val activityRepository = FakeActivityRepository(hasLoggedActivities = false)
        val appLaunchRepository = FakeAppLaunchRepository(hasBootstrapped = true)
        val useCase = EnsureInitialActivityUseCase(
            activityRepository = activityRepository,
            appLaunchRepository = appLaunchRepository,
            timeProvider = FakeTimeProvider(now = 42L),
        )

        val result = useCase()

        assertEquals(EnsureInitialActivityUseCase.DEFAULT_INITIAL_ACTIVITY_TITLE, result?.title)
        assertEquals(EnsureInitialActivityUseCase.DEFAULT_INITIAL_ACTIVITY_TITLE, activityRepository.loggedTitles.single())
        assertEquals(0, appLaunchRepository.markCompletedCalls)
    }

    @Test
    fun invoke_marks_bootstrap_complete_without_logging_when_history_exists() = runTest {
        val activityRepository = FakeActivityRepository(hasLoggedActivities = true)
        val appLaunchRepository = FakeAppLaunchRepository(hasBootstrapped = false)
        val useCase = EnsureInitialActivityUseCase(
            activityRepository = activityRepository,
            appLaunchRepository = appLaunchRepository,
            timeProvider = FakeTimeProvider(now = 42L),
        )

        val result = useCase()

        assertNull(result)
        assertEquals(emptyList<String>(), activityRepository.loggedTitles)
        assertEquals(1, appLaunchRepository.markCompletedCalls)
    }

    private class FakeActivityRepository(
        private val hasLoggedActivities: Boolean,
    ) : ActivityRepository {
        val loggedTitles = mutableListOf<String>()

        override suspend fun hasLoggedActivities(): Boolean = hasLoggedActivities

        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()

        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()

        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()

        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
            loggedTitles += command.title
            return Activity(
                id = 1L,
                title = command.title,
                startTimeEpochMillis = command.timestampEpochMillis,
                endTimeEpochMillis = null,
                status = ActivityStatus.ACTIVE,
                isSyncedToD1 = false,
            )
        }

        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit

        override suspend fun clearAll() = Unit
    }

    private class FakeAppLaunchRepository(
        private val hasBootstrapped: Boolean,
    ) : AppLaunchRepository {
        var markCompletedCalls = 0

        override suspend fun hasCompletedInitialStopwatchBootstrap(): Boolean = hasBootstrapped

        override suspend fun markInitialStopwatchBootstrapCompleted() {
            markCompletedCalls += 1
        }
    }

    private class FakeTimeProvider(
        private val now: Long,
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = now

        override fun currentDayStartEpochMillis(): Long = 0L
    }
}
