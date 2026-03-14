package com.humans.aura.features.day_summary.presentation

import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryReflection

data class DaySummarySectionModel(
    val title: String,
    val items: List<String>,
)

fun DaySummary.toSectionModels(): List<DaySummarySectionModel> {
    val reflection = reflection ?: return emptyList()
    return buildList {
        if (reflection.wins.isNotEmpty()) {
            add(DaySummarySectionModel(title = "Wins", items = reflection.wins))
        }
        if (reflection.frictionPoints.isNotEmpty()) {
            add(DaySummarySectionModel(title = "Friction", items = reflection.frictionPoints))
        }
        if (reflection.tomorrowPivot.isNotBlank()) {
            add(DaySummarySectionModel(title = "Tomorrow Pivot", items = listOf(reflection.tomorrowPivot)))
        }
    }
}
