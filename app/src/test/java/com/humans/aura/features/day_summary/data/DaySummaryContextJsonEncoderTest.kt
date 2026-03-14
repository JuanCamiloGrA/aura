package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryContext
import com.humans.aura.core.domain.models.DaySummaryReflection
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

class DaySummaryContextJsonEncoderTest {

    private val encoder = DaySummaryContextJsonEncoder(Json)

    @Test
    fun encode_includes_core_context_fields() {
        val json = encoder.encode(
            DaySummaryContext(
                dayStartEpochMillis = 100L,
                activities = listOf(Activity(1, "Focus", 10L, 20L, ActivityStatus.ACCURATE, false)),
                dailyGoal = DailyGoal(
                    id = 5L,
                    dayStartEpochMillis = 100L,
                    mainTitle = "Ship milestone",
                    subtasks = listOf(GoalSubtask(7L, 5L, "Coverage", true, 0, false)),
                    isSyncedToD1 = false,
                ),
                recentSummaries = listOf(
                    DaySummary(
                        id = 9L,
                        dayStartEpochMillis = 90L,
                        summaryText = "{\"wins\":[\"Closed the loop\"],\"friction_points\":[\"Meetings\"],\"tomorrow_pivot\":\"Protect the first hour.\"}",
                        reflection = DaySummaryReflection(
                            wins = listOf("Closed the loop"),
                            frictionPoints = listOf("Meetings"),
                            tomorrowPivot = "Protect the first hour.",
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
                    ),
                ),
                completionRatio = 1f,
                focusMinutes = 60L,
                lostMinutes = 0L,
                longestActivityTitle = "Focus",
            ),
        )

        assertTrue(json.contains("\"activities\""))
        assertTrue(json.contains("\"dailyGoal\""))
        assertTrue(json.contains("\"recentSummaries\""))
        assertTrue(json.contains("\"generationStatus\":\"COMPLETED\""))
        assertTrue(json.contains("\"title\":\"Coverage\""))
    }
}
