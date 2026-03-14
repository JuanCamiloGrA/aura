package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveTodayGoalUseCaseTest {

    @Test
    fun invoke_filters_blank_subtasks() = runTest {
        val repository = FakeDailyGoalRepository()

        SaveTodayGoalUseCase(repository).invoke(
            mainTitle = "Ship MVP",
            subtasks = listOf(
                GoalSubtaskDraft("First", false),
                GoalSubtaskDraft("   ", false),
            ),
        )

        assertEquals("Ship MVP", repository.mainTitle)
        assertEquals(listOf("First"), repository.subtasks.map { it.title })
    }

    @Test
    fun invoke_rejects_blank_main_title() = runTest {
        val error = runCatching {
            SaveTodayGoalUseCase(FakeDailyGoalRepository()).invoke("   ", emptyList())
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        var mainTitle: String? = null
        var subtasks: List<GoalSubtaskDraft> = emptyList()

        override fun observeTodayGoal(): Flow<DailyGoal?> = emptyFlow()

        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = null

        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) {
            this.mainTitle = mainTitle
            this.subtasks = subtasks
        }

        override suspend fun clearTodayGoal() = Unit
    }
}
