package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.BackupDocumentRepository
import com.humans.aura.core.domain.interfaces.BackupRepository
import com.humans.aura.core.domain.interfaces.TimeProvider

class ExportBackupToDocumentUseCase(
    private val backupRepository: BackupRepository,
    private val backupDocumentRepository: BackupDocumentRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(documentId: String) {
        val bytes = backupRepository.exportBackup(
            exportedAtEpochMillis = timeProvider.currentTimeMillis(),
        )
        backupDocumentRepository.write(documentId, bytes)
    }
}
