package com.humans.aura.core.domain.models

data class DaySummary(
    val id: Long,
    val dayStartEpochMillis: Long,
    val summaryText: String?,
    val reflection: DaySummaryReflection? = null,
    val rawContextJson: String,
    val promptVersion: String,
    val modelName: String,
    val generationStatus: SummaryGenerationStatus,
    val errorMessage: String?,
    val lastAttemptEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)

fun DaySummary.contextSnippet(): String? =
    reflection?.contextSnippet() ?: summaryText?.trim()?.takeIf { it.isNotEmpty() }

fun DaySummary.previewText(): String? = contextSnippet()

fun DaySummaryReflection.contextSnippet(): String {
    val parts = buildList {
        if (wins.isNotEmpty()) {
            add("Wins: ${wins.joinToString(separator = "; ")}")
        }
        if (frictionPoints.isNotEmpty()) {
            add("Friction: ${frictionPoints.joinToString(separator = "; ")}")
        }
        if (tomorrowPivot.isNotBlank()) {
            add("Tomorrow Pivot: ${tomorrowPivot.trim()}")
        }
    }
    return parts.joinToString(separator = " | ")
}
