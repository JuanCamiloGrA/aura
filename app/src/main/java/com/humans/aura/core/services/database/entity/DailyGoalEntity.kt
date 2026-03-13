package com.humans.aura.core.services.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_goals",
    indices = [Index(value = ["day_start_epoch_millis"], unique = true)],
)
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "day_start_epoch_millis")
    val dayStartEpochMillis: Long,
    @ColumnInfo(name = "main_title")
    val mainTitle: String,
    @ColumnInfo(name = "is_ai_generation_pending")
    val isAiGenerationPending: Boolean = false,
    @ColumnInfo(name = "is_synced_to_d1")
    val isSyncedToD1: Boolean = false,
)
