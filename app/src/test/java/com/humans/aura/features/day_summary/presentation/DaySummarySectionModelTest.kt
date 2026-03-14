package com.humans.aura.features.day_summary.presentation

import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryReflection
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DaySummarySectionModelTest {

    @Test
    fun to_section_models_builds_sections_in_order() {
        val summary = DaySummary(
            id = 1L,
            dayStartEpochMillis = 100L,
            summaryText = "{}",
            reflection = DaySummaryReflection(
                wins = listOf("Protected focus"),
                frictionPoints = listOf("Meetings fragmented flow"),
                tomorrowPivot = "Start with the hardest task.",
            ),
            rawContextJson = "{}",
            promptVersion = "v1",
            modelName = "gemini-test",
            generationStatus = SummaryGenerationStatus.COMPLETED,
            errorMessage = null,
            lastAttemptEpochMillis = 1L,
            createdAtEpochMillis = 1L,
            updatedAtEpochMillis = 1L,
            isSyncedToD1 = false,
        )

        val sections = summary.toSectionModels()

        assertEquals(listOf("Wins", "Friction", "Tomorrow Pivot"), sections.map { it.title })
        assertEquals("Start with the hardest task.", sections.last().items.single())
    }

    @Test
    fun to_section_models_returns_empty_for_missing_reflection() {
        val summary = DaySummary(
            id = 2L,
            dayStartEpochMillis = 100L,
            summaryText = null,
            reflection = null,
            rawContextJson = "{}",
            promptVersion = "v1",
            modelName = "gemini-test",
            generationStatus = SummaryGenerationStatus.PENDING,
            errorMessage = null,
            lastAttemptEpochMillis = null,
            createdAtEpochMillis = 1L,
            updatedAtEpochMillis = 1L,
            isSyncedToD1 = false,
        )

        assertEquals(emptyList<DaySummarySectionModel>(), summary.toSectionModels())
    }

    @Test
    fun to_section_models_skips_empty_groups() {
        val summary = DaySummary(
            id = 3L,
            dayStartEpochMillis = 100L,
            summaryText = "{}",
            reflection = DaySummaryReflection(
                wins = emptyList(),
                frictionPoints = listOf("Meetings"),
                tomorrowPivot = "",
            ),
            rawContextJson = "{}",
            promptVersion = "v1",
            modelName = "gemini-test",
            generationStatus = SummaryGenerationStatus.COMPLETED,
            errorMessage = null,
            lastAttemptEpochMillis = 1L,
            createdAtEpochMillis = 1L,
            updatedAtEpochMillis = 1L,
            isSyncedToD1 = false,
        )

        val sections = summary.toSectionModels()

        assertEquals(listOf("Friction"), sections.map { it.title })
    }

    @Test
    fun to_section_models_skips_blank_tomorrow_pivot_and_empty_friction() {
        val summary = DaySummary(
            id = 4L,
            dayStartEpochMillis = 100L,
            summaryText = "{}",
            reflection = DaySummaryReflection(
                wins = listOf("Closed loops"),
                frictionPoints = emptyList(),
                tomorrowPivot = "   ",
            ),
            rawContextJson = "{}",
            promptVersion = "v1",
            modelName = "gemini-test",
            generationStatus = SummaryGenerationStatus.COMPLETED,
            errorMessage = null,
            lastAttemptEpochMillis = 1L,
            createdAtEpochMillis = 1L,
            updatedAtEpochMillis = 1L,
            isSyncedToD1 = false,
        )

        val sections = summary.toSectionModels()

        assertEquals(listOf("Wins"), sections.map { it.title })
    }
}
