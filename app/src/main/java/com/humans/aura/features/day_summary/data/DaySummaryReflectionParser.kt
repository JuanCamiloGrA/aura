package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.models.DaySummaryReflection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DaySummaryReflectionParser(
    private val json: Json,
) {
    fun parse(summaryText: String?): DaySummaryReflection? {
        if (summaryText.isNullOrBlank()) {
            return null
        }

        return runCatching {
            json.decodeFromString(DaySummaryReflectionPayload.serializer(), summaryText)
        }.getOrNull()?.toDomain()
    }
}

@Serializable
private data class DaySummaryReflectionPayload(
    val wins: List<String> = emptyList(),
    @SerialName("friction_points")
    val frictionPoints: List<String> = emptyList(),
    @SerialName("tomorrow_pivot")
    val tomorrowPivot: String = "",
) {
    fun toDomain(): DaySummaryReflection = DaySummaryReflection(
        wins = wins.filter { it.isNotBlank() },
        frictionPoints = frictionPoints.filter { it.isNotBlank() },
        tomorrowPivot = tomorrowPivot.trim(),
    )
}
