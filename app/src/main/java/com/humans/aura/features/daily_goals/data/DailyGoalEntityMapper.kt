package com.humans.aura.features.daily_goals.data

import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity

fun DailyGoalWithSubtasks.toDomain(): DailyGoal = DailyGoal(
    id = goal.id,
    dayStartEpochMillis = goal.dayStartEpochMillis,
    mainTitle = goal.mainTitle,
    subtasks = subtasks
        .sortedBy(GoalSubtaskEntity::position)
        .map(GoalSubtaskEntity::toDomain),
    isSyncedToD1 = goal.isSyncedToD1,
)

fun GoalSubtaskEntity.toDomain(): GoalSubtask = GoalSubtask(
    id = id,
    goalId = goalId,
    title = title,
    isCompleted = isCompleted,
    position = position,
    isSyncedToD1 = isSyncedToD1,
)
