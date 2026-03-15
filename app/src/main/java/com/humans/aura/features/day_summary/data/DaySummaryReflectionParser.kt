package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.interfaces.DaySummaryReflectionCodec
import com.humans.aura.core.domain.models.DaySummaryReflection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DaySummaryReflectionParser(
    private val json: Json,
) : DaySummaryReflectionCodec {
    override
    fun parse(summaryText: String?): DaySummaryReflection? {
        if (summaryText.isNullOrBlank()) {
            return null
        }

        val jsonCandidate = extractJsonObject(summaryText) ?: return null
        val payload = runCatching {
            json.parseToJsonElement(jsonCandidate).jsonObject.toPayload()
        }.getOrNull() ?: return null
        return payload.toDomain().takeIf { reflection ->
            reflection.wins.isNotEmpty() || reflection.frictionPoints.isNotEmpty() || reflection.tomorrowPivot.isNotBlank()
        }
    }

    override
    fun encode(reflection: DaySummaryReflection): String =
        json.encodeToString(DaySummaryReflectionPayload.from(reflection))

    private fun extractJsonObject(source: String): String? {
        val trimmed = source.trim()
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            val inner = trimmed
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
                .removePrefix("json")
                .trim()
            if (inner.startsWith("{") && inner.endsWith("}")) {
                return inner
            }
        }

        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start == -1 || end == -1 || start >= end) {
            return null
        }
        return trimmed.substring(start, end + 1)
    }

    private fun JsonObject.toPayload(): DaySummaryReflectionPayload = DaySummaryReflectionPayload(
        wins = stringListFor("wins"),
        frictionPoints = stringListFor("friction_points", "frictionPoints"),
        tomorrowPivot = stringFor("tomorrow_pivot", "tomorrowPivot"),
    )

    private fun JsonObject.stringListFor(vararg keys: String): List<String> =
        keys.asSequence()
            .mapNotNull { key -> this[key] }
            .mapNotNull { element -> element.asStringListOrNull() }
            .firstOrNull()
            .orEmpty()

    private fun JsonObject.stringFor(vararg keys: String): String =
        keys.asSequence()
            .mapNotNull { key -> this[key] }
            .mapNotNull { element -> element.asTrimmedStringOrNull() }
            .firstOrNull()
            .orEmpty()

    private fun JsonElement.asStringListOrNull(): List<String>? =
        (this as? JsonArray)?.mapNotNull { element -> element.asTrimmedStringOrNull() }

    private fun JsonElement.asTrimmedStringOrNull(): String? =
        (this as? JsonPrimitive)?.content?.trim()?.takeIf { it.isNotEmpty() }
}

@Serializable
private data class DaySummaryReflectionPayload(
    val wins: List<String> = emptyList(),
    @SerialName("friction_points")
    val frictionPoints: List<String> = emptyList(),
    @SerialName("tomorrow_pivot")
    val tomorrowPivot: String = "",
) {
    companion object {
        fun from(reflection: DaySummaryReflection): DaySummaryReflectionPayload = DaySummaryReflectionPayload(
            wins = reflection.wins,
            frictionPoints = reflection.frictionPoints,
            tomorrowPivot = reflection.tomorrowPivot,
        )
    }

    fun toDomain(): DaySummaryReflection = DaySummaryReflection(
        wins = wins.map(String::trim).filter { it.isNotBlank() },
        frictionPoints = frictionPoints.map(String::trim).filter { it.isNotBlank() },
        tomorrowPivot = tomorrowPivot.trim(),
    )
}
