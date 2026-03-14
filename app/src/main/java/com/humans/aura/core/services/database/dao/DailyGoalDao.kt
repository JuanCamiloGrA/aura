package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.humans.aura.core.services.database.DailyGoalWithSubtasks
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {
    @Transaction
    @Query("SELECT * FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    fun observeGoalForDay(dayStartEpochMillis: Long): Flow<DailyGoalWithSubtasks?>

    @Query("SELECT * FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoalEntity?

    @Transaction
    @Query("SELECT * FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    suspend fun getGoalWithSubtasksForDay(dayStartEpochMillis: Long): DailyGoalWithSubtasks?

    @Insert
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Insert
    suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>)

    @Query("DELETE FROM goal_subtasks WHERE goal_id = :goalId")
    suspend fun deleteSubtasksForGoal(goalId: Long)

    @Transaction
    suspend fun saveGoalWithSubtasks(
        dayStartEpochMillis: Long,
        mainTitle: String,
        subtasks: List<GoalSubtaskEntity>,
    ) {
        val existingGoal = getGoalForDay(dayStartEpochMillis)
        val goalId = if (existingGoal == null) {
            insertGoal(
                DailyGoalEntity(
                    dayStartEpochMillis = dayStartEpochMillis,
                    mainTitle = mainTitle,
                    isSyncedToD1 = false,
                ),
            )
        } else {
            updateGoal(
                existingGoal.copy(
                    mainTitle = mainTitle,
                    isSyncedToD1 = false,
                ),
            )
            deleteSubtasksForGoal(existingGoal.id)
            existingGoal.id
        }

        if (subtasks.isNotEmpty()) {
            insertSubtasks(subtasks.map { subtask -> subtask.copy(goalId = goalId) })
        }
    }

    @Query("DELETE FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis")
    suspend fun deleteGoalForDay(dayStartEpochMillis: Long)
}
