package com.humans.aura.features.configuration.data

import androidx.room.withTransaction
import com.humans.aura.core.services.database.AuraDatabase

class RoomBackupTransactionRunner(
    private val database: AuraDatabase,
) : BackupTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        database.withTransaction {
            block()
        }
}
