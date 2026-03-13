package com.humans.aura.core.services.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_subtasks",
    foreignKeys = [
        ForeignKey(
            entity = DailyGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["goal_id"])],
)
data class GoalSubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "goal_id")
    val goalId: Long,
    val title: String,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    val position: Int,
    @ColumnInfo(name = "is_synced_to_d1")
    val isSyncedToD1: Boolean = false,
)
