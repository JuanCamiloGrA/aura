package com.humans.aura.core.services.database

import androidx.room.Embedded
import androidx.room.Relation
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity

data class DailyGoalWithSubtasks(
    @Embedded val goal: DailyGoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goal_id",
    )
    val subtasks: List<GoalSubtaskEntity>,
)
