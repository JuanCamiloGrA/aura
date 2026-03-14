package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.services.database.dao.DaySummaryDao
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomDaySummaryRepositoryTest {

    @Test
    fun observe_methods_map_entities_to_domain() = runTest {
        val entity = summaryEntity(id = 1, dayStartEpochMillis = 2_000)
        val dao = FakeDaySummaryDao(
            latest = entity,
            recent = listOf(entity),
        )
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(5_000))

        assertEquals(1L, repository.observeLatestSummary().first()?.id)
        assertEquals(listOf(1L), repository.observeRecentSummaries(limit = 5).first().map { it.id })
    }

    @Test
    fun create_pending_summary_returns_existing_when_present() = runTest {
        val existing = summaryEntity(id = 3, dayStartEpochMillis = 1_000)
        val dao = FakeDaySummaryDao(byDayStart = mutableMapOf(1_000L to existing), byId = mutableMapOf(3L to existing))
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(5_000))

        val result = repository.createPendingSummary(1_000)

        assertEquals(3L, result.id)
        assertEquals(0, dao.insertCalls)
    }

    @Test
    fun create_pending_summary_inserts_new_pending_record() = runTest {
        val dao = FakeDaySummaryDao()
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(5_000))

        val result = repository.createPendingSummary(1_000)

        assertEquals(1L, result.id)
        assertEquals(SummaryGenerationStatus.PENDING, result.generationStatus)
        assertEquals("m2-day-summary-v1", result.promptVersion)
        assertEquals("pending", result.modelName)
        assertEquals(5_000L, result.createdAtEpochMillis)
        assertEquals(5_000L, result.updatedAtEpochMillis)
        assertEquals(1, dao.insertCalls)
    }

    @Test
    fun get_pending_summaries_maps_pending_entities() = runTest {
        val pending = listOf(summaryEntity(id = 4), summaryEntity(id = 5))
        val dao = FakeDaySummaryDao(pending = pending)
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(9_000))

        val result = repository.getPendingSummaries(limit = 2)

        assertEquals(listOf(4L, 5L), result.map { it.id })
    }

    @Test
    fun update_pending_context_updates_retry_metadata_and_clears_error() = runTest {
        val existing = summaryEntity(id = 1, errorMessage = "old")
        val dao = FakeDaySummaryDao(byId = mutableMapOf(1L to existing))
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(0))

        repository.updatePendingContext(1, "{\"x\":1}", "v2", "gemini", 77)

        val updated = dao.byId.getValue(1)
        assertEquals("{\"x\":1}", updated.rawContextJson)
        assertEquals("v2", updated.promptVersion)
        assertEquals("gemini", updated.modelName)
        assertEquals(77L, updated.lastAttemptEpochMillis)
        assertEquals(77L, updated.updatedAtEpochMillis)
        assertNull(updated.errorMessage)
        assertEquals(false, updated.isSyncedToD1)
    }

    @Test
    fun update_summary_result_marks_summary_completed() = runTest {
        val existing = summaryEntity(id = 1)
        val dao = FakeDaySummaryDao(byId = mutableMapOf(1L to existing))
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(0))

        repository.updateSummaryResult(1, "done", "gemini", 88)

        val updated = dao.byId.getValue(1)
        assertEquals("done", updated.summaryText)
        assertEquals("gemini", updated.modelName)
        assertEquals(SummaryGenerationStatus.COMPLETED.name, updated.generationStatus)
        assertNull(updated.errorMessage)
        assertEquals(88L, updated.lastAttemptEpochMillis)
    }

    @Test
    fun record_retryable_failure_keeps_pending_status() = runTest {
        val existing = summaryEntity(id = 1)
        val dao = FakeDaySummaryDao(byId = mutableMapOf(1L to existing))
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(0))

        repository.recordRetryableFailure(1, "network", "gemini", 99)

        val updated = dao.byId.getValue(1)
        assertEquals(SummaryGenerationStatus.PENDING.name, updated.generationStatus)
        assertEquals("network", updated.errorMessage)
        assertEquals("gemini", updated.modelName)
        assertEquals(99L, updated.lastAttemptEpochMillis)
    }

    @Test
    fun record_terminal_failure_marks_failed_status() = runTest {
        val existing = summaryEntity(id = 1)
        val dao = FakeDaySummaryDao(byId = mutableMapOf(1L to existing))
        val repository = RoomDaySummaryRepository(dao, FakeTimeProvider(0))

        repository.recordTerminalFailure(1, "fatal", "gemini", 111)

        val updated = dao.byId.getValue(1)
        assertEquals(SummaryGenerationStatus.FAILED.name, updated.generationStatus)
        assertEquals("fatal", updated.errorMessage)
        assertEquals("gemini", updated.modelName)
        assertEquals(111L, updated.lastAttemptEpochMillis)
    }

    private class FakeDaySummaryDao(
        latest: DailySummaryEntity? = null,
        recent: List<DailySummaryEntity> = emptyList(),
        pending: List<DailySummaryEntity> = emptyList(),
        val byId: MutableMap<Long, DailySummaryEntity> = mutableMapOf(),
        private val byDayStart: MutableMap<Long, DailySummaryEntity> = mutableMapOf(),
    ) : DaySummaryDao {
        private val latestFlow = MutableStateFlow(latest)
        private val recentFlow = MutableStateFlow(recent)
        private val pendingEntities = pending
        var insertCalls = 0

        override fun observeLatestSummary(): Flow<DailySummaryEntity?> = latestFlow

        override fun observeRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>> = MutableStateFlow(recentFlow.value.take(limit))

        override suspend fun getSummariesByStatus(status: String, limit: Int): List<DailySummaryEntity> = pendingEntities.take(limit)

        override suspend fun getById(summaryId: Long): DailySummaryEntity? = byId[summaryId]

        override suspend fun getByDayStart(dayStartEpochMillis: Long): DailySummaryEntity? = byDayStart[dayStartEpochMillis]

        override suspend fun insert(summary: DailySummaryEntity): Long {
            insertCalls += 1
            val id = (byId.keys.maxOrNull() ?: 0L) + 1L
            val inserted = summary.copy(id = id)
            byId[id] = inserted
            byDayStart[inserted.dayStartEpochMillis] = inserted
            latestFlow.value = inserted
            recentFlow.value = listOf(inserted) + recentFlow.value
            return id
        }

        override suspend fun update(summary: DailySummaryEntity) {
            byId[summary.id] = summary
            byDayStart[summary.dayStartEpochMillis] = summary
            latestFlow.value = summary
            recentFlow.value = recentFlow.value.map { if (it.id == summary.id) summary else it }
        }
    }

    private class FakeTimeProvider(
        private val now: Long,
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = now

        override fun currentDayStartEpochMillis(): Long = 0
    }

    private fun summaryEntity(
        id: Long = 0,
        dayStartEpochMillis: Long = 1_000,
        summaryText: String? = null,
        rawContextJson: String = "{}",
        promptVersion: String = "v1",
        modelName: String = "pending",
        generationStatus: String = SummaryGenerationStatus.PENDING.name,
        errorMessage: String? = null,
        lastAttemptEpochMillis: Long? = null,
        createdAtEpochMillis: Long = 10,
        updatedAtEpochMillis: Long = 10,
        isSyncedToD1: Boolean = false,
    ) = DailySummaryEntity(
        id = id,
        dayStartEpochMillis = dayStartEpochMillis,
        summaryText = summaryText,
        rawContextJson = rawContextJson,
        promptVersion = promptVersion,
        modelName = modelName,
        generationStatus = generationStatus,
        errorMessage = errorMessage,
        lastAttemptEpochMillis = lastAttemptEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        isSyncedToD1 = isSyncedToD1,
    )
}
