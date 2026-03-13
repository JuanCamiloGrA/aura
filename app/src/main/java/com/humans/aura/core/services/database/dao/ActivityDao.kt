package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.humans.aura.core.services.database.ActivityPredictionEntity
import com.humans.aura.core.services.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query(
        "SELECT * FROM activities WHERE end_time_epoch_millis IS NULL ORDER BY start_time_epoch_millis DESC LIMIT 1",
    )
    fun observeCurrentActivity(): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities ORDER BY start_time_epoch_millis DESC LIMIT :limit")
    fun observeRecentActivities(limit: Int): Flow<List<ActivityEntity>>

    @Query(
        "SELECT * FROM activities WHERE start_time_epoch_millis >= :dayStartEpochMillis AND start_time_epoch_millis < :dayEndEpochMillis ORDER BY start_time_epoch_millis DESC",
    )
    fun observeActivitiesForDay(
        dayStartEpochMillis: Long,
        dayEndEpochMillis: Long,
    ): Flow<List<ActivityEntity>>

    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ActivityEntity?

    @Query(
        "UPDATE activities SET end_time_epoch_millis = :timestampEpochMillis, status = CASE WHEN status = 'ACTIVE' THEN 'ACCURATE' ELSE status END, is_synced_to_d1 = 0 WHERE end_time_epoch_millis IS NULL",
    )
    suspend fun closeOpenActivities(timestampEpochMillis: Long): Int

    @Transaction
    suspend fun logNewActivity(
        title: String,
        timestampEpochMillis: Long,
    ): Long {
        closeOpenActivities(timestampEpochMillis)
        return insert(
            ActivityEntity(
                title = title,
                startTimeEpochMillis = timestampEpochMillis,
                endTimeEpochMillis = null,
                status = "ACTIVE",
                isSyncedToD1 = false,
            ),
        )
    }

    @Query(
        "SELECT title, COUNT(*) AS occurrencesCount, MAX(start_time_epoch_millis) AS lastSeenEpochMillis FROM activities WHERE end_time_epoch_millis IS NOT NULL AND start_time_epoch_millis >= :historyStartEpochMillis AND start_time_epoch_millis < :currentEpochMillis AND ABS((start_time_epoch_millis % :dayDurationMillis) - :timeOfDayEpochMillis) <= :windowMillis GROUP BY title ORDER BY occurrencesCount DESC, lastSeenEpochMillis DESC LIMIT 1",
    )
    suspend fun findPrediction(
        historyStartEpochMillis: Long,
        currentEpochMillis: Long,
        dayDurationMillis: Long,
        timeOfDayEpochMillis: Long,
        windowMillis: Long,
    ): ActivityPredictionEntity?

    @Query(
        "UPDATE activities SET status = :status, is_synced_to_d1 = 0 WHERE id = (SELECT id FROM activities WHERE end_time_epoch_millis IS NULL ORDER BY start_time_epoch_millis DESC LIMIT 1)",
    )
    suspend fun updateCurrentActivityStatus(status: String): Int

    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(activities: List<ActivityEntity>)
}
