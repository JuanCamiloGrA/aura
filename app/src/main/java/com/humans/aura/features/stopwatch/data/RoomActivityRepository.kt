package com.humans.aura.features.stopwatch.data

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.entity.ActivityEntity
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomActivityRepository(
    private val activityDao: ActivityDao,
    private val timeProvider: TimeProvider,
    private val intentMediator: IntentMediator,
) : ActivityRepository {

    override fun observeCurrentActivity(): Flow<Activity?> =
        activityDao.observeCurrentActivity().map { entity ->
            entity?.toDomain()
        }

    override fun observeRecentActivities(limit: Int): Flow<List<Activity>> =
        activityDao.observeRecentActivities(limit).map { entities ->
            entities.map(ActivityEntity::toDomain)
        }

    override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> =
        activityDao.observeActivitiesForDay(
            dayStartEpochMillis = dayStartEpochMillis,
            dayEndEpochMillis = dayStartEpochMillis + DAY_DURATION_MILLIS,
        ).map { entities ->
            entities.map(ActivityEntity::toDomain)
        }

    override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
        val normalizedTitle = command.title.trim()
        val newId = activityDao.logNewActivity(
            title = normalizedTitle,
            timestampEpochMillis = command.timestampEpochMillis,
        )
        val newActivity = requireNotNull(activityDao.getById(newId))

        if (normalizedTitle.equals(SLEEP_KEYWORD, ignoreCase = true)) {
            intentMediator.emit(
                AppIntent.SleepLogged(
                    activityTitle = normalizedTitle,
                    occurredAtEpochMillis = command.timestampEpochMillis,
                ),
            )
        }

        return newActivity.toDomain()
    }

    override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? {
        val prediction = activityDao.findPrediction(
            historyStartEpochMillis = nowEpochMillis - PREDICTION_LOOKBACK_MILLIS,
            currentEpochMillis = nowEpochMillis,
            dayDurationMillis = DAY_DURATION_MILLIS,
            timeOfDayEpochMillis = nowEpochMillis % DAY_DURATION_MILLIS,
            windowMillis = PREDICTION_WINDOW_MILLIS,
        ) ?: return null

        return ActivityPrediction(
            title = prediction.title,
            occurrencesCount = prediction.occurrencesCount,
            lastSeenEpochMillis = prediction.lastSeenEpochMillis,
        )
    }

    override suspend fun updateCurrentActivityStatus(status: ActivityStatus) {
        activityDao.updateCurrentActivityStatus(status.name)
    }

    override suspend fun clearAll() {
        activityDao.deleteAll()
    }

    companion object {
        private const val DAY_DURATION_MILLIS = 86_400_000L
        private const val PREDICTION_LOOKBACK_MILLIS = DAY_DURATION_MILLIS * 7
        private const val PREDICTION_WINDOW_MILLIS = 60 * 60 * 1000L
        private const val SLEEP_KEYWORD = "Sleep"
    }
}
