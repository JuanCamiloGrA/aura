package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveTodayGoalUseCaseTest {

    @Test
    fun invoke_filters_blank_subtasks_and_updates_wallpaper() = runTest {
        val repository = FakeDailyGoalRepository()
        val wallpaperController = FakeWallpaperController()

        SaveTodayGoalUseCase(repository, wallpaperController).invoke(
            mainTitle = "Ship MVP",
            subtasks = listOf(
                GoalSubtaskDraft("First", false),
                GoalSubtaskDraft("   ", false),
            ),
        )

        assertEquals("Ship MVP", repository.mainTitle)
        assertEquals(listOf("First"), repository.subtasks.map { it.title })
        assertEquals("Ship MVP", wallpaperController.workTitles.single())
    }

    @Test
    fun invoke_rejects_blank_main_title() = runTest {
        val error = runCatching {
            SaveTodayGoalUseCase(FakeDailyGoalRepository(), FakeWallpaperController()).invoke("   ", emptyList())
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        var mainTitle: String? = null
        var subtasks: List<GoalSubtaskDraft> = emptyList()

        override fun observeTodayGoal(): Flow<DailyGoal?> = emptyFlow()

        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = null

        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) {
            this.mainTitle = mainTitle
            this.subtasks = subtasks
        }

        override suspend fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) = Unit

        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeWallpaperController : WallpaperController {
        val workTitles = mutableListOf<String>()

        override suspend fun setWorkModeWallpaper(title: String) {
            workTitles += title
        }

        override suspend fun setNightModeWallpaper() = Unit
    }
}
