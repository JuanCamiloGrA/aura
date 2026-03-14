package com.humans.aura.features.day_summary.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DaySummaryReflectionParserTest {

    private val parser = DaySummaryReflectionParser(
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        },
    )

    @Test
    fun parse_returns_structured_reflection_for_valid_json() {
        val reflection = parser.parse(
            """
            {"wins":["Protected focus","Closed the day"],"friction_points":["Context switching"],"tomorrow_pivot":"Start with planning."}
            """.trimIndent(),
        )

        assertEquals(listOf("Protected focus", "Closed the day"), reflection?.wins)
        assertEquals(listOf("Context switching"), reflection?.frictionPoints)
        assertEquals("Start with planning.", reflection?.tomorrowPivot)
    }

    @Test
    fun parse_returns_null_for_plain_text() {
        assertNull(parser.parse("Strong day overall"))
    }
}
