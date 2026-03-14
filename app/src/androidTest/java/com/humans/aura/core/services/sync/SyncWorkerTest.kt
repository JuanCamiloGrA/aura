package com.humans.aura.core.services.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import com.humans.aura.AuraApplication
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
import com.humans.aura.features.day_summary.domain.AssembleDaySummaryContextUseCase
import com.humans.aura.features.day_summary.domain.BuildDaySummaryPromptUseCase
import com.humans.aura.features.day_summary.domain.GeneratePendingDaySummariesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private lateinit var application: AuraApplication

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Unit
    }

    @Test
    fun do_work_returns_success_when_generation_succeeds() = runTest {
        withTestUseCase(testUseCaseFor(aiBehavior = { AiResponse("summary", "gemini-test") })) {
            val result = buildWorker().doWork()

            assertTrue(result is ListenableWorker.Result.Success)
        }
    }

    @Test
    fun do_work_returns_retry_when_generation_hits_offline_state() = runTest {
        withTestUseCase(testUseCaseFor(aiBehavior = { throw IOException("offline") })) {
            val result = buildWorker().doWork()

            assertTrue(result is ListenableWorker.Result.Retry)
        }
    }

    @Test
    fun do_work_returns_failure_when_generation_is_terminal() = runTest {
        withTestUseCase(testUseCaseFor(aiBehavior = { throw AiGenerationException.NonRetryable("bad request") })) {
            val result = buildWorker().doWork()

            assertTrue(result is ListenableWorker.Result.Failure)
            assertEquals(
                "Day summary generation failed",
                (result as ListenableWorker.Result.Failure).outputData.getString(SyncWorker.KEY_ERROR_MESSAGE),
            )
        }
    }

    private fun buildWorker(): SyncWorker =
        androidx.work.testing.TestListenableWorkerBuilder
            .from(application as Context, SyncWorker::class.java)
            .setWorkerFactory(workerFactory ?: error("WorkerFactory must be initialized"))
            .build()

    private var workerFactory: WorkerFactory? = null

    private suspend fun withTestUseCase(
        useCase: GeneratePendingDaySummariesUseCase,
        block: suspend () -> Unit,
    ) {
        workerFactory = AuraWorkerFactory(useCase)
        block()
    }

    private fun testUseCaseFor(
        aiBehavior: suspend (AiRequest) -> AiResponse,
    ): GeneratePendingDaySummariesUseCase = GeneratePendingDaySummariesUseCase(
        daySummaryRepository = FakeDaySummaryRepository(),
        assembleDaySummaryContextUseCase = AssembleDaySummaryContextUseCase(
            conversationContextRepository = FakeConversationContextRepository(),
            timeProvider = FakeTimeProvider(),
        ),
        buildDaySummaryPromptUseCase = BuildDaySummaryPromptUseCase(Json),
        aiTextGenerator = object : AiTextGenerator {
            override suspend fun generate(request: AiRequest): AiResponse = aiBehavior(request)
        },
        timeProvider = FakeTimeProvider(),
    )

    private class FakeConversationContextRepository : ConversationContextRepository {
        override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext = DaySummaryContext(
            dayStartEpochMillis = dayStartEpochMillis,
            activities = listOf(Activity(1, "Focus", dayStartEpochMillis, dayStartEpochMillis + 60_000L, ActivityStatus.ACCURATE, false)),
            dailyGoal = null,
            recentSummaries = emptyList(),
            completionRatio = 1f,
            focusMinutes = 60L,
            lostMinutes = 0L,
            longestActivityTitle = "Focus",
        )

        override suspend fun buildChatContext(limit: Int): DaySummaryContext = buildContextForDay(100L)
    }

    private class FakeDaySummaryRepository : DaySummaryRepository {
        override fun observeLatestSummary(): Flow<DaySummary?> = emptyFlow()
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = emptyFlow()

        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary = error("unused")

        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = listOf(
            DaySummary(
                id = 1L,
                dayStartEpochMillis = 100L,
                summaryText = null,
                rawContextJson = "{}",
                promptVersion = "v1",
                modelName = "pending",
                generationStatus = SummaryGenerationStatus.PENDING,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = 100L,
                updatedAtEpochMillis = 100L,
                isSyncedToD1 = false,
            ),
        )

        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 999L
        override fun currentDayStartEpochMillis(): Long = 100L
    }
}
