package com.humans.aura.core.services.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkManagerSyncSchedulerTest {

    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build(),
        )
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun schedule_day_closure_sync_enqueues_work() {
        val scheduler = WorkManagerSyncScheduler(workManager)

        scheduler.scheduleDayClosureSync()

        val infos = workManager.getWorkInfosByTag(SyncWorker::class.java.name).get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.SUCCEEDED, infos.first().state)
    }
}
