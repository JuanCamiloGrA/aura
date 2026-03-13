package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeCurrentActivity(): Flow<Activity?>

    fun observeRecentActivities(limit: Int = 5): Flow<List<Activity>>

    fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>>

    suspend fun logNewActivity(command: LogNewActivityCommand): Activity

    suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction?

    suspend fun updateCurrentActivityStatus(status: ActivityStatus)

    suspend fun clearAll()
}
