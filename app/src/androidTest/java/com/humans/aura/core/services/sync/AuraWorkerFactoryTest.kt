package com.humans.aura.core.services.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuraWorkerFactoryTest {

    @Test
    fun create_worker_returns_sync_worker_for_known_class() = runTest {
        val workerFactory = AuraWorkerFactory(testUseCase())
        val worker = TestListenableWorkerBuilder
            .from(ApplicationProvider.getApplicationContext<Context>(), SyncWorker::class.java)
            .setWorkerFactory(workerFactory)
            .build()

        assertTrue(worker is SyncWorker)
    }

    @Test
    fun supports_returns_false_for_unknown_class() {
        val workerFactory = AuraWorkerFactory(testUseCase())

        assertFalse(workerFactory.supports("unknown.Worker"))
    }

    private fun testUseCase(): GeneratePendingDaySummariesUseCase = GeneratePendingDaySummariesUseCase(
        daySummaryRepository = object : DaySummaryRepository {
            override fun observeLatestSummary(): Flow<DaySummary?> = emptyFlow()
            override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = emptyFlow()
            override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary = error("unused")
            override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()
            override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
            override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
            override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
            override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        },
        assembleDaySummaryContextUseCase = AssembleDaySummaryContextUseCase(
            conversationContextRepository = object : ConversationContextRepository {
                override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext = DaySummaryContext(
                    dayStartEpochMillis = dayStartEpochMillis,
                    activities = listOf(Activity(1, "Focus", 0L, 1L, ActivityStatus.ACCURATE, false)),
                    dailyGoal = null,
                    recentSummaries = emptyList(),
                    completionRatio = 1f,
                    focusMinutes = 1L,
                    lostMinutes = 0L,
                    longestActivityTitle = "Focus",
                )

                override suspend fun buildChatContext(limit: Int): DaySummaryContext = buildContextForDay(0L)
            },
            timeProvider = object : TimeProvider {
                override fun currentTimeMillis(): Long = 1L
                override fun currentDayStartEpochMillis(): Long = 0L
            },
        ),
        buildDaySummaryPromptUseCase = BuildDaySummaryPromptUseCase(Json),
        aiTextGenerator = object : AiTextGenerator {
            override suspend fun generate(request: AiRequest): AiResponse = AiResponse("ok", "gemini-test")
        },
        timeProvider = object : TimeProvider {
            override fun currentTimeMillis(): Long = 1L
            override fun currentDayStartEpochMillis(): Long = 0L
        },
    )
}
