package com.humans.aura.core.domain.models

data class AuraBackupSummary(
    val exportedAtEpochMillis: Long,
    val activitiesCount: Int,
    val dailyGoalsCount: Int,
    val goalSubtasksCount: Int,
    val daySummariesCount: Int,
    val chatSessionsCount: Int,
    val chatMessagesCount: Int,
)
