package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaySummaryDao {
    @Query("SELECT * FROM daily_summaries ORDER BY day_start_epoch_millis DESC LIMIT 1")
    fun observeLatestSummary(): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries ORDER BY day_start_epoch_millis DESC LIMIT :limit")
    fun observeRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE generation_status = :status ORDER BY day_start_epoch_millis ASC LIMIT :limit")
    suspend fun getSummariesByStatus(
        status: String,
        limit: Int,
    ): List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries WHERE id = :summaryId LIMIT 1")
    suspend fun getById(summaryId: Long): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    suspend fun getByDayStart(dayStartEpochMillis: Long): DailySummaryEntity?

    @Insert
    suspend fun insert(summary: DailySummaryEntity): Long

    @Update
    suspend fun update(summary: DailySummaryEntity)
}
