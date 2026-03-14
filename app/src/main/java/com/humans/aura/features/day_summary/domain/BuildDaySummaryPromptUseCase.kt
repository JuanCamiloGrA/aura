package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.models.DaySummaryContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BuildDaySummaryPromptUseCase(
    private val json: Json,
) {
    operator fun invoke(context: DaySummaryContext): String = buildString {
        appendLine("Analyze the user's day honestly.")
        appendLine("Return a concise reflective summary covering wins, misses, goal alignment, and tomorrow guidance.")
        append(json.encodeToString(DaySummaryContextPayload.from(context)))
    }
}

@kotlinx.serialization.Serializable
private data class DaySummaryContextPayload(
    val dayStartEpochMillis: Long,
    val activityTitles: List<String>,
    val goalTitle: String?,
    val completionRatio: Float,
    val focusMinutes: Long,
    val lostMinutes: Long,
    val longestActivityTitle: String?,
    val previousSummarySnippets: List<String>,
) {
    companion object {
        fun from(context: DaySummaryContext): DaySummaryContextPayload = DaySummaryContextPayload(
            dayStartEpochMillis = context.dayStartEpochMillis,
            activityTitles = context.activities.map { it.title },
            goalTitle = context.dailyGoal?.mainTitle,
            completionRatio = context.completionRatio,
            focusMinutes = context.focusMinutes,
            lostMinutes = context.lostMinutes,
            longestActivityTitle = context.longestActivityTitle,
            previousSummarySnippets = context.recentSummaries.mapNotNull { it.summaryText },
        )
    }
}
