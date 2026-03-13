package com.humans.aura.core.domain.models

data class GoalSubtask(
    val id: Long,
    val goalId: Long,
    val title: String,
    val isCompleted: Boolean,
    val position: Int,
    val isSyncedToD1: Boolean,
)
