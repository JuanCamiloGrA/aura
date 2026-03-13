package com.humans.aura.features.daily_goals.data

import app.cash.turbine.test
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomDailyGoalRepositoryTest {

    @Test
    fun observe_today_goal_maps_relation() = runTest {
        val dao = FakeDailyGoalDao(
            relation = DailyGoalWithSubtasks(
                DailyGoalEntity(1, 0L, "Ship MVP", true, false),
                listOf(GoalSubtaskEntity(1, 1, "Scope", false, 0)),
            ),
        )
        val repository = RoomDailyGoalRepository(dao, FakeTimeProvider())

        repository.observeTodayGoal().test {
            assertEquals("Ship MVP", awaitItem()?.mainTitle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_today_goal_trims_inputs_and_builds_subtasks() = runTest {
        val dao = FakeDailyGoalDao(null)
        val repository = RoomDailyGoalRepository(dao, FakeTimeProvider())

        repository.saveTodayGoal(
            mainTitle = "  Win the day  ",
            subtasks = listOf(GoalSubtaskDraft(" First ", false)),
        )

        assertEquals("Win the day", dao.savedMainTitle)
        assertEquals(listOf("First"), dao.savedSubtasks.map { it.title })
    }

    private class FakeDailyGoalDao(
        relation: DailyGoalWithSubtasks?,
    ) : DailyGoalDao {
        private val flow = MutableStateFlow(relation)
        var savedMainTitle: String? = null
        var savedSubtasks: List<GoalSubtaskEntity> = emptyList()

        override fun observeGoalForDay(dayStartEpochMillis: Long): Flow<DailyGoalWithSubtasks?> = flow.asStateFlow()
        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoalEntity? = null
        override suspend fun insertGoal(goal: DailyGoalEntity): Long = 1L
        override suspend fun updateGoal(goal: DailyGoalEntity) = Unit
        override suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>) = Unit
        override suspend fun deleteSubtasksForGoal(goalId: Long) = Unit

        override suspend fun saveGoalWithSubtasks(dayStartEpochMillis: Long, mainTitle: String, subtasks: List<GoalSubtaskEntity>) {
            savedMainTitle = mainTitle
            savedSubtasks = subtasks
        }

        override suspend fun markAiGenerationPending(dayStartEpochMillis: Long) = Unit
        override suspend fun deleteGoalForDay(dayStartEpochMillis: Long) = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }
}
