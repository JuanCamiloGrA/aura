package com.humans.aura.features.configuration.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.models.AuraBackupSummary
import com.humans.aura.features.configuration.domain.CreateBackupFileNameUseCase
import com.humans.aura.features.configuration.domain.ExportBackupToDocumentUseCase
import com.humans.aura.features.configuration.domain.RestoreBackupFromDocumentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun export_backup_updates_success_message() = runTest {
        var exportedDocumentId: String? = null
        val viewModel = ConfigurationViewModel(
            createBackupFileNameUseCase = CreateBackupFileNameUseCase(FakeTimeProvider()),
            exportBackupToDocumentUseCase = ExportBackupToDocumentUseCase(
                backupRepository = FakeBackupRepository(),
                backupDocumentRepository = FakeBackupDocumentRepository(onWrite = { documentId, _ ->
                    exportedDocumentId = documentId
                }),
                timeProvider = FakeTimeProvider(),
            ),
            restoreBackupFromDocumentUseCase = RestoreBackupFromDocumentUseCase(
                backupRepository = FakeBackupRepository(),
                backupDocumentRepository = FakeBackupDocumentRepository(),
            ),
        )

        viewModel.exportBackup("backup://target")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("backup://target", exportedDocumentId)
            assertFalse(state.isExporting)
            assertEquals("Backup saved as a .aura archive.", state.statusMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun restore_backup_surfaces_summary() = runTest {
        val summary = AuraBackupSummary(99L, 4, 1, 2, 3, 1, 8)
        val viewModel = ConfigurationViewModel(
            createBackupFileNameUseCase = CreateBackupFileNameUseCase(FakeTimeProvider()),
            exportBackupToDocumentUseCase = ExportBackupToDocumentUseCase(
                backupRepository = FakeBackupRepository(),
                backupDocumentRepository = FakeBackupDocumentRepository(),
                timeProvider = FakeTimeProvider(),
            ),
            restoreBackupFromDocumentUseCase = RestoreBackupFromDocumentUseCase(
                backupRepository = FakeBackupRepository(summary = summary),
                backupDocumentRepository = FakeBackupDocumentRepository(readBytes = byteArrayOf(1)),
            ),
        )

        viewModel.restoreBackup("backup://source")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isRestoring)
            assertEquals(summary, state.lastRestoredSummary)
            assertEquals("Backup restored. Local data now matches the archive.", state.statusMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun restore_backup_surfaces_errors() = runTest {
        val viewModel = ConfigurationViewModel(
            createBackupFileNameUseCase = CreateBackupFileNameUseCase(FakeTimeProvider()),
            exportBackupToDocumentUseCase = ExportBackupToDocumentUseCase(
                backupRepository = FakeBackupRepository(),
                backupDocumentRepository = FakeBackupDocumentRepository(),
                timeProvider = FakeTimeProvider(),
            ),
            restoreBackupFromDocumentUseCase = RestoreBackupFromDocumentUseCase(
                backupRepository = FakeBackupRepository(restoreError = IllegalStateException("Broken archive")),
                backupDocumentRepository = FakeBackupDocumentRepository(readBytes = byteArrayOf(1)),
            ),
        )

        viewModel.restoreBackup("backup://source")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isStatusError)
        assertEquals("Broken archive", state.statusMessage)
    }

    private class FakeTimeProvider : com.humans.aura.core.domain.interfaces.TimeProvider {
        override fun currentTimeMillis(): Long = 1_736_728_496_000
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeBackupRepository(
        private val summary: AuraBackupSummary = AuraBackupSummary(0L, 0, 0, 0, 0, 0, 0),
        private val restoreError: Throwable? = null,
    ) : com.humans.aura.core.domain.interfaces.BackupRepository {
        override suspend fun exportBackup(exportedAtEpochMillis: Long): ByteArray = byteArrayOf(1, 2, 3)

        override suspend fun restoreBackup(bytes: ByteArray): AuraBackupSummary {
            restoreError?.let { throw it }
            return summary
        }
    }

    private class FakeBackupDocumentRepository(
        private val onWrite: (suspend (String, ByteArray) -> Unit)? = null,
        private val readBytes: ByteArray = byteArrayOf(),
    ) : com.humans.aura.core.domain.interfaces.BackupDocumentRepository {
        override suspend fun write(documentId: String, bytes: ByteArray) {
            onWrite?.invoke(documentId, bytes)
        }

        override suspend fun read(documentId: String): ByteArray = readBytes
    }
}
