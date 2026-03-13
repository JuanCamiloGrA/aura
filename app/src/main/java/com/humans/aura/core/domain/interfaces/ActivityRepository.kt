package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeCurrentActivity(): Flow<Activity?>

    fun observeRecentActivities(limit: Int = 5): Flow<List<Activity>>

    suspend fun seedPhaseZeroData()

    suspend fun clearAll()
}
