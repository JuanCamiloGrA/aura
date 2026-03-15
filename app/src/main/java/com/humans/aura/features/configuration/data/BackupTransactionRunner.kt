package com.humans.aura.features.configuration.data

interface BackupTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
