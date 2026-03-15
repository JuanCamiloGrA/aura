package com.humans.aura.features.configuration.domain

import com.humans.aura.core.domain.interfaces.BackupDocumentRepository
import com.humans.aura.core.domain.interfaces.BackupRepository
import com.humans.aura.core.domain.models.AuraBackupSummary

class RestoreBackupFromDocumentUseCase(
    private val backupRepository: BackupRepository,
    private val backupDocumentRepository: BackupDocumentRepository,
) {
    suspend operator fun invoke(documentId: String): AuraBackupSummary {
        val bytes = backupDocumentRepository.read(documentId)
        return backupRepository.restoreBackup(bytes)
    }
}
