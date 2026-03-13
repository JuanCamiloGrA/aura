package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogNewActivityUseCaseTest {

    @Test
    fun invoke_trims_title_and_logs_with_current_time() = runTest {
        val repository = FakeActivityRepository()
        val useCase = LogNewActivityUseCase(repository, FakeTimeProvider(250L))

        useCase("  Focus block  ")

        assertEquals("Focus block", repository.lastCommand?.title)
        assertEquals(250L, repository.lastCommand?.timestampEpochMillis)
    }

    @Test
    fun invoke_rejects_blank_titles() = runTest {
        val useCase = LogNewActivityUseCase(FakeActivityRepository(), FakeTimeProvider(250L))

        val error = runCatching { useCase("   ") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeActivityRepository : ActivityRepository {
        var lastCommand: LogNewActivityCommand? = null

        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()

        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
            lastCommand = command
            return Activity(1, command.title, command.timestampEpochMillis, null, ActivityStatus.ACTIVE, false)
        }

        override suspend fun predictNextTitle(nowEpochMillis: Long) = null
        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit
        override suspend fun clearAll() = Unit
    }

    private class FakeTimeProvider(
        private val now: Long,
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = now
        override fun currentDayStartEpochMillis(): Long = 0L
    }
}
