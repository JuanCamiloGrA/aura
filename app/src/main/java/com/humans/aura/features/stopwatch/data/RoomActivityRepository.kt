package com.humans.aura.features.stopwatch.data

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActivityRepository(
    private val activityDao: ActivityDao,
    private val timeProvider: TimeProvider,
) : ActivityRepository {

    override fun observeCurrentActivity(): Flow<com.humans.aura.core.domain.models.Activity?> =
        activityDao.observeCurrentActivity().map { entity ->
            entity?.toDomain()
        }

    override fun observeRecentActivities(limit: Int): Flow<List<com.humans.aura.core.domain.models.Activity>> =
        activityDao.observeRecentActivities(limit).map { entities ->
            entities.map(ActivityEntity::toDomain)
        }

    override suspend fun seedPhaseZeroData() {
        if (activityDao.count() > 0) {
            return
        }

        val now = timeProvider.currentTimeMillis()
        val minute = 60_000L

        activityDao.insertAll(
            listOf(
                ActivityEntity(
                    title = "Morning planning",
                    startTimeEpochMillis = now - 360L * minute,
                    endTimeEpochMillis = now - 300L * minute,
                    status = ActivityStatus.ACCURATE.name,
                ),
                ActivityEntity(
                    title = "Backlog refinement",
                    startTimeEpochMillis = now - 285L * minute,
                    endTimeEpochMillis = now - 210L * minute,
                    status = ActivityStatus.INACCURATE.name,
                ),
                ActivityEntity(
                    title = "Ghost time recovered",
                    startTimeEpochMillis = now - 195L * minute,
                    endTimeEpochMillis = now - 165L * minute,
                    status = ActivityStatus.LOST.name,
                ),
                ActivityEntity(
                    title = "Phase 0 foundation",
                    startTimeEpochMillis = now - 90L * minute,
                    endTimeEpochMillis = null,
                    status = ActivityStatus.ACTIVE.name,
                ),
            ),
        )
    }

    override suspend fun clearAll() {
        activityDao.deleteAll()
    }
}
