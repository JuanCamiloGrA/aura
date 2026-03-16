package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityPrediction
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCurrentActivityTitleUseCaseTest {

    @Test
    fun invoke_trims_title_and_delegates_to_repository() = runTest {
        val repository = FakeActivityRepository()

        UpdateCurrentActivityTitleUseCase(repository).invoke("  Deep work  ")

        assertEquals("Deep work", repository.updatedTitle)
    }

    @Test
    fun invoke_rejects_blank_title() = runTest {
        val error = runCatching {
            UpdateCurrentActivityTitleUseCase(FakeActivityRepository()).invoke("   ")
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeActivityRepository : ActivityRepository {
        var updatedTitle: String? = null

        override suspend fun hasLoggedActivities(): Boolean = false
        override fun observeCurrentActivity(): Flow<Activity?> = emptyFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = emptyFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = emptyFlow()
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = error("unused")
        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null
        override suspend fun updateCurrentActivityTitle(title: String) {
            updatedTitle = title
        }
        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit
    }
}
