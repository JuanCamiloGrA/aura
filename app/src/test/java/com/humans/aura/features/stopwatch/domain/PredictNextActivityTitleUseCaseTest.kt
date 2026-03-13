package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PredictNextActivityTitleUseCaseTest {

    @Test
    fun invoke_uses_repository_prediction_for_current_time() = runTest {
        val repository = FakeActivityRepository()
        val prediction = PredictNextActivityTitleUseCase(repository, FakeTimeProvider(999L)).invoke()

        assertEquals(999L, repository.requestedTime)
        assertEquals("Review", prediction?.title)
    }

    private class FakeActivityRepository : ActivityRepository {
        var requestedTime: Long? = null

        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = throw UnsupportedOperationException()

        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction {
            requestedTime = nowEpochMillis
            return ActivityPrediction("Review", 3, 500L)
        }

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
