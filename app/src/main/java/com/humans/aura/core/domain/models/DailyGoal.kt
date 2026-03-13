package com.humans.aura.core.domain.models

data class DailyGoal(
    val id: Long,
    val dayStartEpochMillis: Long,
    val mainTitle: String,
    val subtasks: List<GoalSubtask>,
    val isSyncedToD1: Boolean,
) {
    val completedSubtasks: Int
        get() = subtasks.count { it.isCompleted }

    val totalSubtasks: Int
        get() = subtasks.size
}
