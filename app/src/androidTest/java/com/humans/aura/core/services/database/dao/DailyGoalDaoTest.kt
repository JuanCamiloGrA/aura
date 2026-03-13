package com.humans.aura.core.services.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.humans.aura.core.services.database.AuraDatabase
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyGoalDaoTest {

    private lateinit var database: AuraDatabase
    private lateinit var dao: DailyGoalDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AuraDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.dailyGoalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun save_goal_with_subtasks_persists_relation() = runTest {
        dao.saveGoalWithSubtasks(
            dayStartEpochMillis = 0L,
            mainTitle = "Win the day",
            subtasks = listOf(
                GoalSubtaskEntity(goalId = 0L, title = "Plan", isCompleted = false, position = 0),
            ),
        )

        val relation = dao.getGoalForDay(0L)
        assertEquals("Win the day", relation?.mainTitle)
    }

    @Test
    fun mark_ai_generation_pending_sets_flag() = runTest {
        dao.markAiGenerationPending(0L)

        assertEquals(true, dao.getGoalForDay(0L)?.isAiGenerationPending)
    }
}
