package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCurrentActivityStatusUseCaseTest {

    @Test
    fun invoke_delegates_terminal_status() = runTest {
        val repository = FakeActivityRepository()
        UpdateCurrentActivityStatusUseCase(repository).invoke(ActivityStatus.LOST)

        assertEquals(ActivityStatus.LOST, repository.status)
    }

    @Test
    fun invoke_rejects_active_status() = runTest {
        val useCase = UpdateCurrentActivityStatusUseCase(FakeActivityRepository())

        val error = runCatching { useCase(ActivityStatus.ACTIVE) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeActivityRepository : ActivityRepository {
        var status: ActivityStatus? = null

        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = throw UnsupportedOperationException()
        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) {
            this.status = status
        }

        override suspend fun clearAll() = Unit
    }
}
