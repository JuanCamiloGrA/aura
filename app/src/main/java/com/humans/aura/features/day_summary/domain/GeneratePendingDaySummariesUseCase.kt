package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiTask
import com.humans.aura.features.day_summary.data.DaySummaryContextJsonEncoder
import com.humans.aura.features.day_summary.data.DaySummaryReflectionParser
import kotlinx.coroutines.CancellationException
import java.io.IOException

class GeneratePendingDaySummariesUseCase(
    private val daySummaryRepository: DaySummaryRepository,
    private val assembleDaySummaryContextUseCase: AssembleDaySummaryContextUseCase,
    private val buildDaySummaryPromptUseCase: BuildDaySummaryPromptUseCase,
    private val daySummaryContextJsonEncoder: DaySummaryContextJsonEncoder,
    private val reflectionParser: DaySummaryReflectionParser,
    private val aiTextGenerator: AiTextGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): DaySummarySyncResult {
        val summaries = daySummaryRepository.getPendingSummaries()
        if (summaries.isEmpty()) {
            return DaySummarySyncResult.SUCCESS
        }

        var result = DaySummarySyncResult.SUCCESS

        summaries.forEach { summary ->
            val context = assembleDaySummaryContextUseCase(summary.dayStartEpochMillis)
            val prompt = buildDaySummaryPromptUseCase(context)
            val rawContextJson = daySummaryContextJsonEncoder.encode(context)
            val now = timeProvider.currentTimeMillis()

            daySummaryRepository.updatePendingContext(
                summaryId = summary.id,
                rawContextJson = rawContextJson,
                promptVersion = PROMPT_VERSION,
                modelName = MODEL_NAME,
                lastAttemptEpochMillis = now,
            )

            runCatching {
                val response = aiTextGenerator.generate(
                    AiRequest(
                        task = AiTask.DAY_SUMMARY,
                        systemInstruction = SYSTEM_INSTRUCTION,
                        prompt = prompt,
                    ),
                )
                val reflection = reflectionParser.parse(response.text)
                    ?: throw AiGenerationException.NonRetryable(
                        "Gemini response did not match the required summary JSON schema",
                    )
                CompletedSummaryResult(
                    summaryText = reflectionParser.encode(reflection),
                    modelName = response.modelName,
                )
            }.onSuccess { completedSummary ->
                daySummaryRepository.updateSummaryResult(
                    summaryId = summary.id,
                    summaryText = completedSummary.summaryText,
                    modelName = completedSummary.modelName,
                    lastAttemptEpochMillis = now,
                )
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }

                val errorMessage = error.message ?: "Unknown summary generation error"
                if (error.isRetryable()) {
                    daySummaryRepository.recordRetryableFailure(
                        summaryId = summary.id,
                        errorMessage = errorMessage,
                        modelName = MODEL_NAME,
                        lastAttemptEpochMillis = now,
                    )
                    result = DaySummarySyncResult.RETRY
                } else {
                    daySummaryRepository.recordTerminalFailure(
                        summaryId = summary.id,
                        errorMessage = errorMessage,
                        modelName = MODEL_NAME,
                        lastAttemptEpochMillis = now,
                    )
                    if (result != DaySummarySyncResult.RETRY) {
                        result = DaySummarySyncResult.FAILURE
                    }
                }
            }
        }

        return result
    }

    private fun Throwable.isRetryable(): Boolean =
        this is IOException || this is AiGenerationException.Retryable

    companion object {
        const val MODEL_NAME = "gemini-2.0-flash-lite"
        const val PROMPT_VERSION = "m3-day-summary-v1"
        const val SYSTEM_INSTRUCTION = "You are AURA, an honest but supportive daily reflection assistant."
    }

    private data class CompletedSummaryResult(
        val summaryText: String,
        val modelName: String,
    )
}
