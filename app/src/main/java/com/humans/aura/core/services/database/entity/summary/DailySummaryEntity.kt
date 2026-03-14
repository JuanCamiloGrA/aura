package com.humans.aura.core.services.database.entity.summary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_summaries",
    indices = [Index(value = ["day_start_epoch_millis"], unique = true)],
)
data class DailySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "day_start_epoch_millis")
    val dayStartEpochMillis: Long,
    @ColumnInfo(name = "summary_text")
    val summaryText: String?,
    @ColumnInfo(name = "raw_context_json")
    val rawContextJson: String,
    @ColumnInfo(name = "prompt_version")
    val promptVersion: String,
    @ColumnInfo(name = "model_name")
    val modelName: String,
    @ColumnInfo(name = "generation_status")
    val generationStatus: String,
    @ColumnInfo(name = "error_message")
    val errorMessage: String?,
    @ColumnInfo(name = "last_attempt_epoch_millis")
    val lastAttemptEpochMillis: Long?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
    @ColumnInfo(name = "is_synced_to_d1")
    val isSyncedToD1: Boolean,
)
