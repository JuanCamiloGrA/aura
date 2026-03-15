package com.humans.aura.core.services.storage

import android.content.Context
import androidx.core.net.toUri
import com.humans.aura.core.domain.interfaces.BackupDocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidBackupDocumentRepository(
    context: Context,
) : BackupDocumentRepository {

    private val contentResolver = context.contentResolver

    override suspend fun write(documentId: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        contentResolver.openOutputStream(documentId.toUri(), "w")?.use { outputStream ->
            outputStream.write(bytes)
            outputStream.flush()
        } ?: error("AURA could not open the selected backup destination.")
    }

    override suspend fun read(documentId: String): ByteArray = withContext(Dispatchers.IO) {
        contentResolver.openInputStream(documentId.toUri())?.use { inputStream ->
            inputStream.readBytes()
        } ?: error("AURA could not open the selected backup file.")
    }
}
