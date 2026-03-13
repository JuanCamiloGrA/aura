package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Insert
    suspend fun insertAll(activities: List<ActivityEntity>)

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun count(): Int

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}
