package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.services.database.dao.DaySummaryDao
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDaySummaryRepository(
    private val daySummaryDao: DaySummaryDao,
    private val timeProvider: TimeProvider,
) : DaySummaryRepository {

    override fun observeLatestSummary(): Flow<DaySummary?> =
        daySummaryDao.observeLatestSummary().map { entity -> entity?.toDomain() }

    override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> =
        daySummaryDao.observeRecentSummaries(limit).map { entities -> entities.map(DailySummaryEntity::toDomain) }

    override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary {
        val existing = daySummaryDao.getByDayStart(dayStartEpochMillis)
        if (existing != null) {
            return existing.toDomain()
        }

        val now = timeProvider.currentTimeMillis()
        val id = daySummaryDao.insert(
            DailySummaryEntity(
                dayStartEpochMillis = dayStartEpochMillis,
                summaryText = null,
                rawContextJson = "{}",
                promptVersion = PROMPT_VERSION,
                modelName = PENDING_MODEL,
                generationStatus = SummaryGenerationStatus.PENDING.name,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                isSyncedToD1 = false,
            ),
        )
        return requireNotNull(daySummaryDao.getById(id)).toDomain()
    }

    override suspend fun getPendingSummaries(limit: Int): List<DaySummary> =
        daySummaryDao.getSummariesByStatus(SummaryGenerationStatus.PENDING.name, limit).map(DailySummaryEntity::toDomain)

    override suspend fun updatePendingContext(
        summaryId: Long,
        rawContextJson: String,
        promptVersion: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    ) {
        val entity = requireNotNull(daySummaryDao.getById(summaryId))
        daySummaryDao.update(
            entity.copy(
                rawContextJson = rawContextJson,
                promptVersion = promptVersion,
                modelName = modelName,
                lastAttemptEpochMillis = lastAttemptEpochMillis,
                updatedAtEpochMillis = lastAttemptEpochMillis,
                errorMessage = null,
                isSyncedToD1 = false,
            ),
        )
    }

    override suspend fun updateSummaryResult(
        summaryId: Long,
        summaryText: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    ) {
        val entity = requireNotNull(daySummaryDao.getById(summaryId))
        daySummaryDao.update(
            entity.copy(
                summaryText = summaryText,
                modelName = modelName,
                generationStatus = SummaryGenerationStatus.COMPLETED.name,
                errorMessage = null,
                lastAttemptEpochMillis = lastAttemptEpochMillis,
                updatedAtEpochMillis = lastAttemptEpochMillis,
                isSyncedToD1 = false,
            ),
        )
    }

    override suspend fun recordRetryableFailure(
        summaryId: Long,
        errorMessage: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    ) {
        val entity = requireNotNull(daySummaryDao.getById(summaryId))
        daySummaryDao.update(
            entity.copy(
                modelName = modelName,
                generationStatus = SummaryGenerationStatus.PENDING.name,
                errorMessage = errorMessage,
                lastAttemptEpochMillis = lastAttemptEpochMillis,
                updatedAtEpochMillis = lastAttemptEpochMillis,
                isSyncedToD1 = false,
            ),
        )
    }

    override suspend fun recordTerminalFailure(
        summaryId: Long,
        errorMessage: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    ) {
        val entity = requireNotNull(daySummaryDao.getById(summaryId))
        daySummaryDao.update(
            entity.copy(
                modelName = modelName,
                generationStatus = SummaryGenerationStatus.FAILED.name,
                errorMessage = errorMessage,
                lastAttemptEpochMillis = lastAttemptEpochMillis,
                updatedAtEpochMillis = lastAttemptEpochMillis,
                isSyncedToD1 = false,
            ),
        )
    }

    companion object {
        private const val PROMPT_VERSION = "m2-day-summary-v1"
        private const val PENDING_MODEL = "pending"
    }
}
