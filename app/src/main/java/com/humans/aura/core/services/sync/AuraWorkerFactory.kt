package com.humans.aura.core.services.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.humans.aura.features.day_summary.domain.GeneratePendingDaySummariesUseCase

class AuraWorkerFactory(
    private val generatePendingDaySummariesUseCase: GeneratePendingDaySummariesUseCase,
) : WorkerFactory() {

    fun supports(workerClassName: String): Boolean = workerClassName == SyncWorker::class.qualifiedName

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParams: WorkerParameters,
    ): ListenableWorker? = when {
        supports(workerClassName) -> SyncWorker(
            appContext = appContext,
            workerParams = workerParams,
            generatePendingDaySummariesUseCase = generatePendingDaySummariesUseCase,
        )

        else -> null
    }
}
