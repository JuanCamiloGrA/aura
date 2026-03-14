package com.humans.aura.core.services.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.humans.aura.features.day_summary.domain.DaySummarySyncResult
import com.humans.aura.features.day_summary.domain.GeneratePendingDaySummariesUseCase

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val generatePendingDaySummariesUseCase: GeneratePendingDaySummariesUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        when (generatePendingDaySummariesUseCase.invoke()) {
            DaySummarySyncResult.SUCCESS -> Result.success()
            DaySummarySyncResult.RETRY -> Result.retry()
            DaySummarySyncResult.FAILURE -> Result.failure(errorData("Day summary generation failed"))
        }
    } catch (error: Exception) {
        Result.failure(errorData(error.message ?: "Unexpected sync failure"))
    }

    private fun errorData(message: String): Data = Data.Builder()
        .putString(KEY_ERROR_MESSAGE, message)
        .build()

    companion object {
        const val KEY_ERROR_MESSAGE = "sync_error_message"
    }
}
