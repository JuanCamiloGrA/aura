package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DaySummaryContext
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GeneratePendingDaySummariesUseCaseTest {

    @Test
    fun invoke_completes_pending_summary_on_success() = runTest {
        val repository = FakeDaySummaryRepository()
        val useCase = createUseCase(
            repository = repository,
            aiTextGenerator = FakeAiTextGenerator { AiResponse("Strong day overall", "gemini-test") },
        )

        val result = useCase()

        assertEquals(DaySummarySyncResult.SUCCESS, result)
        assertEquals(1, repository.pendingContextUpdates.size)
        assertEquals("Strong day overall", repository.completedResults.single().summaryText)
    }

    @Test
    fun invoke_marks_retryable_failure_and_returns_retry() = runTest {
        val repository = FakeDaySummaryRepository()
        val useCase = createUseCase(
            repository = repository,
            aiTextGenerator = FakeAiTextGenerator { throw IOException("offline") },
        )

        val result = useCase()

        assertEquals(DaySummarySyncResult.RETRY, result)
        assertEquals("offline", repository.retryableFailures.single().errorMessage)
    }

    @Test
    fun invoke_marks_terminal_failure_and_returns_failure() = runTest {
        val repository = FakeDaySummaryRepository()
        val useCase = createUseCase(
            repository = repository,
            aiTextGenerator = FakeAiTextGenerator {
                throw AiGenerationException.NonRetryable("bad prompt")
            },
        )

        val result = useCase()

        assertEquals(DaySummarySyncResult.FAILURE, result)
        assertEquals("bad prompt", repository.terminalFailures.single().errorMessage)
    }

    @Test
    fun invoke_uses_each_summary_day_context() = runTest {
        val repository = FakeDaySummaryRepository(
            pendingSummaries = listOf(
                fakeSummary(id = 1, dayStartEpochMillis = 100L),
                fakeSummary(id = 2, dayStartEpochMillis = 200L),
            ),
        )
        val contextRepository = FakeConversationContextRepository()
        val useCase = GeneratePendingDaySummariesUseCase(
            daySummaryRepository = repository,
            assembleDaySummaryContextUseCase = AssembleDaySummaryContextUseCase(
                conversationContextRepository = contextRepository,
                timeProvider = FakeTimeProvider(),
            ),
            buildDaySummaryPromptUseCase = BuildDaySummaryPromptUseCase(Json),
            aiTextGenerator = FakeAiTextGenerator { AiResponse("ok", "gemini-test") },
            timeProvider = FakeTimeProvider(),
        )

        useCase()

        assertEquals(listOf(100L, 200L), contextRepository.requestedDays)
        assertTrue(repository.pendingContextUpdates.all { it.rawContextJson.contains("dayStartEpochMillis") })
    }

    private fun createUseCase(
        repository: FakeDaySummaryRepository,
        aiTextGenerator: AiTextGenerator,
    ): GeneratePendingDaySummariesUseCase = GeneratePendingDaySummariesUseCase(
        daySummaryRepository = repository,
        assembleDaySummaryContextUseCase = AssembleDaySummaryContextUseCase(
            conversationContextRepository = FakeConversationContextRepository(),
            timeProvider = FakeTimeProvider(),
        ),
        buildDaySummaryPromptUseCase = BuildDaySummaryPromptUseCase(Json),
        aiTextGenerator = aiTextGenerator,
        timeProvider = FakeTimeProvider(),
    )

    private fun fakeSummary(
        id: Long = 1L,
        dayStartEpochMillis: Long = 100L,
    ): DaySummary = DaySummary(
        id = id,
        dayStartEpochMillis = dayStartEpochMillis,
        summaryText = null,
        rawContextJson = "{}",
        promptVersion = "v1",
        modelName = "pending",
        generationStatus = SummaryGenerationStatus.PENDING,
        errorMessage = null,
        lastAttemptEpochMillis = null,
        createdAtEpochMillis = dayStartEpochMillis,
        updatedAtEpochMillis = dayStartEpochMillis,
        isSyncedToD1 = false,
    )

    private class FakeConversationContextRepository : ConversationContextRepository {
        val requestedDays = mutableListOf<Long>()

        override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext {
            requestedDays += dayStartEpochMillis
            return DaySummaryContext(
                dayStartEpochMillis = dayStartEpochMillis,
                activities = listOf(Activity(1, "Focus", dayStartEpochMillis, dayStartEpochMillis + 60_000L, ActivityStatus.ACCURATE, false)),
                dailyGoal = null,
                recentSummaries = emptyList(),
                completionRatio = 0.5f,
                focusMinutes = 60L,
                lostMinutes = 0L,
                longestActivityTitle = "Focus",
            )
        }

        override suspend fun buildChatContext(limit: Int): DaySummaryContext = buildContextForDay(100L)
    }

    private class FakeAiTextGenerator(
        private val block: suspend (AiRequest) -> AiResponse,
    ) : AiTextGenerator {
        override suspend fun generate(request: AiRequest): AiResponse = block(request)
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 999L
        override fun currentDayStartEpochMillis(): Long = 100L
    }

    private class FakeDaySummaryRepository(
        private val pendingSummaries: List<DaySummary> = listOf(defaultSummary()),
    ) : DaySummaryRepository {
        val pendingContextUpdates = mutableListOf<PendingContextUpdate>()
        val completedResults = mutableListOf<CompletedResult>()
        val retryableFailures = mutableListOf<FailureRecord>()
        val terminalFailures = mutableListOf<FailureRecord>()

        override fun observeLatestSummary(): Flow<DaySummary?> = emptyFlow()
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = emptyFlow()
        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary = defaultSummary(dayStartEpochMillis = dayStartEpochMillis)
        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = pendingSummaries.take(limit)

        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) {
            pendingContextUpdates += PendingContextUpdate(summaryId, rawContextJson, promptVersion, modelName, lastAttemptEpochMillis)
        }

        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) {
            completedResults += CompletedResult(summaryId, summaryText, modelName, lastAttemptEpochMillis)
        }

        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) {
            retryableFailures += FailureRecord(summaryId, errorMessage, modelName, lastAttemptEpochMillis)
        }

        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) {
            terminalFailures += FailureRecord(summaryId, errorMessage, modelName, lastAttemptEpochMillis)
        }

        companion object {
            private fun defaultSummary(
                id: Long = 1L,
                dayStartEpochMillis: Long = 100L,
            ): DaySummary = DaySummary(
                id = id,
                dayStartEpochMillis = dayStartEpochMillis,
                summaryText = null,
                rawContextJson = "{}",
                promptVersion = "v1",
                modelName = "pending",
                generationStatus = SummaryGenerationStatus.PENDING,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = dayStartEpochMillis,
                updatedAtEpochMillis = dayStartEpochMillis,
                isSyncedToD1 = false,
            )
        }
    }

    private data class PendingContextUpdate(
        val summaryId: Long,
        val rawContextJson: String,
        val promptVersion: String,
        val modelName: String,
        val lastAttemptEpochMillis: Long,
    )

    private data class CompletedResult(
        val summaryId: Long,
        val summaryText: String,
        val modelName: String,
        val lastAttemptEpochMillis: Long,
    )

    private data class FailureRecord(
        val summaryId: Long,
        val errorMessage: String,
        val modelName: String,
        val lastAttemptEpochMillis: Long,
    )
}
