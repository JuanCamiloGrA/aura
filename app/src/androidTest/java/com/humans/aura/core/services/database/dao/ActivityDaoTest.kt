package com.humans.aura.core.services.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.humans.aura.core.services.database.AuraDatabase
import com.humans.aura.core.services.database.entity.ActivityEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityDaoTest {

    private lateinit var database: AuraDatabase
    private lateinit var dao: ActivityDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AuraDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.activityDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun log_new_activity_closes_previous_and_opens_new_one_atomically() = runTest {
        dao.insert(
            ActivityEntity(
                title = "Planning",
                startTimeEpochMillis = 10L,
                endTimeEpochMillis = null,
                status = "ACTIVE",
                isSyncedToD1 = false,
            ),
        )

        val newId = dao.logNewActivity("Coding", 100L)
        val newActivity = dao.getById(newId)

        assertNotNull(newActivity)
        val activities = listOfNotNull(dao.getById(newId), dao.getById(1L)).sortedByDescending { it.startTimeEpochMillis }
        assertEquals(100L, activities.first().startTimeEpochMillis)
        assertEquals(100L, activities.last().endTimeEpochMillis)
    }

    @Test
    fun prediction_prefers_frequent_title() = runTest {
        repeat(3) { index ->
            dao.insert(
                ActivityEntity(
                    title = "Review",
                    startTimeEpochMillis = 86_400_000L * (index + 1) + 36_000_000L,
                    endTimeEpochMillis = 86_400_000L * (index + 1) + 39_600_000L,
                    status = "ACCURATE",
                ),
            )
        }

        val prediction = dao.findPrediction(
            historyStartEpochMillis = 0L,
            currentEpochMillis = 86_400_000L * 7,
            dayDurationMillis = 86_400_000L,
            timeOfDayEpochMillis = 36_000_000L,
            windowMillis = 3_600_000L,
        )

        assertEquals("Review", prediction?.title)
    }
}
