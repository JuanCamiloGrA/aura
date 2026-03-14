package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DaySummaryEntityMapperTest {

    @Test
    fun to_domain_maps_all_fields() {
        val entity = DailySummaryEntity(
            id = 7,
            dayStartEpochMillis = 1000,
            summaryText = "A reflective day",
            rawContextJson = "{\"mood\":\"steady\"}",
            promptVersion = "m2-day-summary-v1",
            modelName = "gemini-2.5-flash",
            generationStatus = SummaryGenerationStatus.COMPLETED.name,
            errorMessage = null,
            lastAttemptEpochMillis = 2000,
            createdAtEpochMillis = 3000,
            updatedAtEpochMillis = 4000,
            isSyncedToD1 = false,
        )

        val domain = entity.toDomain()

        assertEquals(7L, domain.id)
        assertEquals(1000L, domain.dayStartEpochMillis)
        assertEquals("A reflective day", domain.summaryText)
        assertEquals("{\"mood\":\"steady\"}", domain.rawContextJson)
        assertEquals("m2-day-summary-v1", domain.promptVersion)
        assertEquals("gemini-2.5-flash", domain.modelName)
        assertEquals(SummaryGenerationStatus.COMPLETED, domain.generationStatus)
        assertEquals(null, domain.errorMessage)
        assertEquals(2000L, domain.lastAttemptEpochMillis)
        assertEquals(3000L, domain.createdAtEpochMillis)
        assertEquals(4000L, domain.updatedAtEpochMillis)
        assertEquals(false, domain.isSyncedToD1)
    }

    @Test
    fun to_domain_with_parser_maps_structured_reflection() {
        val entity = DailySummaryEntity(
            id = 8,
            dayStartEpochMillis = 1000,
            summaryText = """{"wins":["Deep work"],"friction_points":["Slack"],"tomorrow_pivot":"Protect the morning."}""",
            rawContextJson = "{}",
            promptVersion = "m3-day-summary-v1",
            modelName = "gemini-2.0-flash-lite",
            generationStatus = SummaryGenerationStatus.COMPLETED.name,
            errorMessage = null,
            lastAttemptEpochMillis = 2000,
            createdAtEpochMillis = 3000,
            updatedAtEpochMillis = 4000,
            isSyncedToD1 = false,
        )

        val domain = entity.toDomain(
            DaySummaryReflectionParser(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            ),
        )

        assertEquals(listOf("Deep work"), domain.reflection?.wins)
        assertEquals(listOf("Slack"), domain.reflection?.frictionPoints)
        assertEquals("Protect the morning.", domain.reflection?.tomorrowPivot)
    }
}
