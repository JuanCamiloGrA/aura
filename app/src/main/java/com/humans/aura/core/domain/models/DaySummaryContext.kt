package com.humans.aura.core.domain.models

data class DaySummaryContext(
    val dayStartEpochMillis: Long,
    val activities: List<Activity>,
    val dailyGoal: DailyGoal?,
    val recentSummaries: List<DaySummary>,
    val completionRatio: Float,
    val focusMinutes: Long,
    val lostMinutes: Long,
    val longestActivityTitle: String?,
)
