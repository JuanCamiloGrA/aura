package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleGoalSubtaskUseCaseTest {

    @Test
    fun invoke_delegates_subtask_toggle_to_repository() = runTest {
        val repository = FakeDailyGoalRepository()

        ToggleGoalSubtaskUseCase(repository).invoke(subtaskId = 42L, isCompleted = true)

        assertEquals(42L to true, repository.toggledSubtask)
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        var toggledSubtask: Pair<Long, Boolean>? = null

        override fun observeTodayGoal(): Flow<DailyGoal?> = emptyFlow()

        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = null

        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) = Unit

        override suspend fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) {
            toggledSubtask = subtaskId to isCompleted
        }

        override suspend fun clearTodayGoal() = Unit
    }
}
