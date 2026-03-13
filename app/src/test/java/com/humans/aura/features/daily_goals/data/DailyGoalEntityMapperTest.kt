package com.humans.aura.features.daily_goals.data

import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyGoalEntityMapperTest {

    @Test
    fun relation_to_domain_sorts_subtasks_and_maps_pending_flag() {
        val relation = DailyGoalWithSubtasks(
            goal = DailyGoalEntity(
                id = 1,
                dayStartEpochMillis = 100L,
                mainTitle = "Protect focus",
                isAiGenerationPending = true,
                isSyncedToD1 = false,
            ),
            subtasks = listOf(
                GoalSubtaskEntity(id = 2, goalId = 1, title = "Second", isCompleted = false, position = 1),
                GoalSubtaskEntity(id = 1, goalId = 1, title = "First", isCompleted = true, position = 0),
            ),
        )

        val domain = relation.toDomain()

        assertEquals(true, domain.isAiGenerationPending)
        assertEquals(listOf("First", "Second"), domain.subtasks.map { it.title })
    }
}
