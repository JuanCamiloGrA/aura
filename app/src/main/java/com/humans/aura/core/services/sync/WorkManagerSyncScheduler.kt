package com.humans.aura.core.services.sync

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.humans.aura.core.domain.interfaces.SyncScheduler

class WorkManagerSyncScheduler(
    private val workManager: WorkManager,
) : SyncScheduler {

    override fun scheduleDayClosureSync() {
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            buildDayClosureSyncRequest(),
        )
    }

    internal fun buildDayClosureSyncRequest(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .addTag(SyncWorker::class.java.name)
            .build()

    companion object {
        const val UNIQUE_WORK_NAME = "day_closure_sync"
    }
}
