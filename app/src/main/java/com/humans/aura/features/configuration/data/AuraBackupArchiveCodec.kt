package com.humans.aura.features.configuration.data

import com.humans.aura.core.domain.models.AuraBackupArchive
import com.humans.aura.features.configuration.domain.InvalidBackupArchiveException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

open class AuraBackupArchiveCodec(
    private val json: Json,
) {

    open fun encode(archive: AuraBackupArchive): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zipOutputStream ->
            zipOutputStream.putNextEntry(ZipEntry(BACKUP_ENTRY_NAME))
            zipOutputStream.write(json.encodeToString(AuraBackupArchive.serializer(), archive).encodeToByteArray())
            zipOutputStream.closeEntry()
        }
        return outputStream.toByteArray()
    }

    open fun decode(bytes: ByteArray): AuraBackupArchive = try {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null && entry.name != BACKUP_ENTRY_NAME) {
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }

            if (entry == null) {
                throw InvalidBackupArchiveException("This file does not contain an AURA backup archive.")
            }

            val archive = json.decodeFromString(
                AuraBackupArchive.serializer(),
                zipInputStream.readBytes().decodeToString(),
            )

            if (archive.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                throw InvalidBackupArchiveException("This backup version is not supported by the current app build.")
            }

            archive
        }
    } catch (exception: InvalidBackupArchiveException) {
        throw exception
    } catch (exception: SerializationException) {
        throw InvalidBackupArchiveException("This file is not a valid AURA backup.", exception)
    } catch (exception: ZipException) {
        throw InvalidBackupArchiveException("This file is not a valid AURA backup.", exception)
    } catch (exception: IllegalArgumentException) {
        throw InvalidBackupArchiveException("This file is not a valid AURA backup.", exception)
    }

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
        private const val BACKUP_ENTRY_NAME = "aura-backup.json"
    }
}
