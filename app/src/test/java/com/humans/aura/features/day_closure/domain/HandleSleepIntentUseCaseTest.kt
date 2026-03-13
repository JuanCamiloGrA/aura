package com.humans.aura.features.day_closure.domain

import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.AppIntent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleSleepIntentUseCaseTest {

    @Test
    fun invoke_sets_wallpaper_and_schedules_sync() = runTest {
        val dailyGoalRepository = FakeDailyGoalRepository()
        val wallpaperController = FakeWallpaperController()
        val syncScheduler = FakeSyncScheduler()

        HandleSleepIntentUseCase(
            dailyGoalRepository = dailyGoalRepository,
            wallpaperController = wallpaperController,
            syncScheduler = syncScheduler,
        ).invoke(AppIntent.SleepLogged("Sleep", 1L))

        assertEquals(1, dailyGoalRepository.calls)
        assertEquals(1, wallpaperController.calls)
        assertEquals(1, syncScheduler.calls)
    }

    private class FakeDailyGoalRepository : com.humans.aura.core.domain.interfaces.DailyGoalRepository {
        var calls = 0

        override fun observeTodayGoal() = kotlinx.coroutines.flow.emptyFlow<com.humans.aura.core.domain.models.DailyGoal?>()
        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<com.humans.aura.core.domain.models.GoalSubtaskDraft>) = Unit
        override suspend fun markAiGenerationPending() {
            calls += 1
        }

        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeWallpaperController : WallpaperController {
        var calls = 0

        override suspend fun setNightModeWallpaper() {
            calls += 1
        }
    }

    private class FakeSyncScheduler : SyncScheduler {
        var calls = 0

        override fun scheduleDayClosureSync() {
            calls += 1
        }
    }
}
