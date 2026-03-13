package com.humans.aura.core.services.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    indices = [
        Index(value = ["start_time_epoch_millis"]),
        Index(value = ["end_time_epoch_millis"]),
    ],
)
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
