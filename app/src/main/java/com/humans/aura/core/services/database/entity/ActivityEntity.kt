package com.humans.aura.core.services.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,
    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long?,
    val status: String,
    @ColumnInfo(name = "is_synced_to_d1")
    val isSyncedToD1: Boolean = false,
)
