package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM daily_goals ORDER BY day_start_epoch_millis ASC")
    suspend fun getAllGoals(): List<DailyGoalEntity>

    @Query("SELECT * FROM goal_subtasks ORDER BY goal_id ASC, position ASC, id ASC")
    suspend fun getAllSubtasks(): List<GoalSubtaskEntity>

    @Transaction
    @Query("SELECT * FROM daily_goals WHERE day_start_epoch_millis = :dayStartEpochMillis LIMIT 1")
    suspend fun getGoalWithSubtasksForDay(dayStartEpochMillis: Long): DailyGoalWithSubtasks?

    @Insert
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoalEntity>)

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Query("UPDATE daily_goals SET main_title = :mainTitle, is_synced_to_d1 = 0 WHERE day_start_epoch_millis = :dayStartEpochMillis")
    suspend fun updateGoalTitle(dayStartEpochMillis: Long, mainTitle: String)

    @Insert
    suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>)

    @Query("UPDATE goal_subtasks SET is_completed = :isCompleted, is_synced_to_d1 = 0 WHERE id = :subtaskId")
    suspend fun updateSubtaskCompletion(subtaskId: Long, isCompleted: Boolean)

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

    @Query("DELETE FROM daily_goals")
    suspend fun deleteAllGoals()
}
