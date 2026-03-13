package com.humans.aura.features.stopwatch.data

import app.cash.turbine.test
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.services.database.ActivityPredictionEntity
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.entity.ActivityEntity
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomActivityRepositoryTest {

    @Test
    fun observe_current_activity_maps_entity() = runTest {
        val dao = FakeActivityDao(current = ActivityEntity(1, "Focus", 10L, null, "ACTIVE", false))
        val repository = RoomActivityRepository(dao, FakeTimeProvider(), FakeIntentMediator())

        repository.observeCurrentActivity().test {
            assertEquals("Focus", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun log_new_activity_emits_sleep_intent_for_sleep_title() = runTest {
        val dao = FakeActivityDao(inserted = ActivityEntity(5, "Sleep", 99L, null, "ACTIVE", false))
        val mediator = FakeIntentMediator()
        val repository = RoomActivityRepository(dao, FakeTimeProvider(), mediator)

        repository.logNewActivity(LogNewActivityCommand("Sleep", 99L))

        assertEquals(AppIntent.SleepLogged("Sleep", 99L), mediator.emitted.single())
    }

    @Test
    fun predict_next_title_maps_prediction_entity() = runTest {
        val dao = FakeActivityDao(prediction = ActivityPredictionEntity("Review", 3, 80L))
        val repository = RoomActivityRepository(dao, FakeTimeProvider(), FakeIntentMediator())

        val prediction = repository.predictNextTitle(100L)

        assertEquals("Review", prediction?.title)
    }

    @Test
    fun predict_next_title_returns_null_when_absent() = runTest {
        val repository = RoomActivityRepository(FakeActivityDao(), FakeTimeProvider(), FakeIntentMediator())

        assertNull(repository.predictNextTitle(100L))
    }

    @Test
    fun update_current_status_delegates_to_dao() = runTest {
        val dao = FakeActivityDao()
        val repository = RoomActivityRepository(dao, FakeTimeProvider(), FakeIntentMediator())

        repository.updateCurrentActivityStatus(ActivityStatus.INACCURATE)

        assertEquals("INACCURATE", dao.updatedStatus)
    }

    private class FakeActivityDao(
        current: ActivityEntity? = null,
        private val inserted: ActivityEntity? = null,
        private val prediction: ActivityPredictionEntity? = null,
    ) : ActivityDao {
        private val currentFlow = MutableStateFlow(current)
        private val recentFlow = MutableStateFlow<List<ActivityEntity>>(emptyList())
        private val dayFlow = MutableStateFlow<List<ActivityEntity>>(emptyList())
        var updatedStatus: String? = null

        override fun observeCurrentActivity(): Flow<ActivityEntity?> = currentFlow.asStateFlow()
        override fun observeRecentActivities(limit: Int): Flow<List<ActivityEntity>> = recentFlow.asStateFlow()
        override fun observeActivitiesForDay(dayStartEpochMillis: Long, dayEndEpochMillis: Long): Flow<List<ActivityEntity>> = dayFlow.asStateFlow()
        override suspend fun insert(activity: ActivityEntity): Long = inserted?.id ?: 1L
        override suspend fun getById(id: Long): ActivityEntity? = inserted
        override suspend fun closeOpenActivities(timestampEpochMillis: Long): Int = 1
        override suspend fun findPrediction(historyStartEpochMillis: Long, currentEpochMillis: Long, dayDurationMillis: Long, timeOfDayEpochMillis: Long, windowMillis: Long): ActivityPredictionEntity? = prediction

        override suspend fun updateCurrentActivityStatus(status: String): Int {
            updatedStatus = status
            return 1
        }

        override suspend fun deleteAll() = Unit
        override suspend fun insertAll(activities: List<ActivityEntity>) = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeIntentMediator : IntentMediator {
        private val mutable = MutableSharedFlow<AppIntent>()
        val emitted = mutableListOf<AppIntent>()
        override val intents = mutable.asSharedFlow()

        override suspend fun emit(intent: AppIntent) {
            emitted += intent
            mutable.emit(intent)
        }
    }
}
