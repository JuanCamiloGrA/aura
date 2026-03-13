package com.humans.aura.features.stopwatch.data

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.services.database.entity.ActivityEntity

fun ActivityEntity.toDomain(): Activity = Activity(
    id = id,
    title = title,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = ActivityStatus.valueOf(status),
    isSyncedToD1 = isSyncedToD1,
)

fun Activity.toEntity(): ActivityEntity = ActivityEntity(
    id = id,
    title = title,
    startTimeEpochMillis = startTimeEpochMillis,
    endTimeEpochMillis = endTimeEpochMillis,
    status = status.name,
    isSyncedToD1 = isSyncedToD1,
)
