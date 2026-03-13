package com.humans.aura.core.services.sync

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.humans.aura.core.domain.interfaces.SyncScheduler

class WorkManagerSyncScheduler(
    private val workManager: WorkManager,
) : SyncScheduler {

    override fun scheduleDayClosureSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(SyncWorker::class.java.name)
            .build()
        workManager.enqueue(request)
    }
}
