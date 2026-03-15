package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.models.ActivityPrediction
import com.humans.aura.core.domain.models.LogNewActivityCommand
import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCurrentActivityStatusUseCaseTest {

    @Test
    fun invoke_delegates_terminal_status() = runTest {
        val repository = FakeActivityRepository()
        UpdateCurrentActivityStatusUseCase(repository).invoke(ActivityStatus.LOST)

        assertEquals(ActivityStatus.LOST, repository.status)
    }

    @Test
    fun invoke_allows_active_status_for_label_removal() = runTest {
        val repository = FakeActivityRepository()

        UpdateCurrentActivityStatusUseCase(repository).invoke(ActivityStatus.ACTIVE)

        assertEquals(ActivityStatus.ACTIVE, repository.status)
    }

    private class FakeActivityRepository : ActivityRepository {
        var status: ActivityStatus? = null

        override suspend fun hasLoggedActivities(): Boolean = false

        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = throw UnsupportedOperationException()
        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) {
            this.status = status
        }
    }
}
