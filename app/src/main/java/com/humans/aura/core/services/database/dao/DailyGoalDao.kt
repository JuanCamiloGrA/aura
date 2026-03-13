package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {
    @Transaction
    @Query("SELECT * FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    fun observeGoalForDay(dayStartEpochMillis: Long): Flow<DailyGoalWithSubtasks?>

    @Insert
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert
    suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>)

    @Query("SELECT COUNT(*) FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis")
    suspend fun countGoalsForDay(dayStartEpochMillis: Long): Int

    @Query("DELETE FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis")
    suspend fun deleteGoalForDay(dayStartEpochMillis: Long)
}
