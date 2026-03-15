package com.humans.aura.core.domain.interfaces

interface BackupDocumentRepository {
    suspend fun write(documentId: String, bytes: ByteArray)

    suspend fun read(documentId: String): ByteArray
}
