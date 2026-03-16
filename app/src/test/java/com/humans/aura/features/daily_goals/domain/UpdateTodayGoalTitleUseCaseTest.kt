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

class UpdateTodayGoalTitleUseCaseTest {

    @Test
    fun invoke_trims_title_updates_repository_and_wallpaper() = runTest {
        val repository = FakeDailyGoalRepository()
        val wallpaperController = FakeWallpaperController()

        UpdateTodayGoalTitleUseCase(repository, wallpaperController).invoke("  Ship milestone  ")

        assertEquals("Ship milestone", repository.updatedTitle)
        assertEquals("Ship milestone", wallpaperController.workTitles.single())
    }

    @Test
    fun invoke_rejects_blank_title() = runTest {
        val error = runCatching {
            UpdateTodayGoalTitleUseCase(FakeDailyGoalRepository(), FakeWallpaperController()).invoke("   ")
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        var updatedTitle: String? = null

        override fun observeTodayGoal(): Flow<DailyGoal?> = emptyFlow()
        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = null
        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) = Unit
        override suspend fun updateTodayGoalTitle(title: String) {
            updatedTitle = title
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
