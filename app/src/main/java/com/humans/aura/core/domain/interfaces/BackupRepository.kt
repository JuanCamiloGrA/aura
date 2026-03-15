package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AuraBackupSummary

interface BackupRepository {
    suspend fun exportBackup(exportedAtEpochMillis: Long): ByteArray

    suspend fun restoreBackup(bytes: ByteArray): AuraBackupSummary
}
