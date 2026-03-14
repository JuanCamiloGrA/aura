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

    @Test
    fun parse_extracts_json_from_markdown_wrapped_response() {
        val reflection = parser.parse(
            """
            Here is the summary:
            ```json
            {"wins":["Closed loops"],"friction_points":["Meetings"],"tomorrow_pivot":"Protect the first hour."}
            ```
            """.trimIndent(),
        )

        assertEquals(listOf("Closed loops"), reflection?.wins)
        assertEquals(listOf("Meetings"), reflection?.frictionPoints)
        assertEquals("Protect the first hour.", reflection?.tomorrowPivot)
    }

    @Test
    fun encode_serializes_structured_reflection_back_to_json() {
        val encoded = parser.encode(
            com.humans.aura.core.domain.models.DaySummaryReflection(
                wins = listOf("Protected focus"),
                frictionPoints = listOf("Slack"),
                tomorrowPivot = "Start offline.",
            ),
        )

        assertEquals(
            "{\"wins\":[\"Protected focus\"],\"friction_points\":[\"Slack\"],\"tomorrow_pivot\":\"Start offline.\"}",
            encoded,
        )
    }
}
