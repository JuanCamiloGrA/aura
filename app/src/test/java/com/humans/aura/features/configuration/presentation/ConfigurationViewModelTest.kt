package com.humans.aura.features.configuration.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.models.AppThemeModePreference
import com.humans.aura.core.domain.models.AuraBackupSummary
import com.humans.aura.features.configuration.domain.CreateBackupFileNameUseCase
import com.humans.aura.features.configuration.domain.ObserveThemeModePreferenceUseCase
import com.humans.aura.features.configuration.domain.ExportBackupToDocumentUseCase
import com.humans.aura.features.configuration.domain.RestoreBackupFromDocumentUseCase
import com.humans.aura.features.configuration.domain.SetThemeModePreferenceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun theme_preference_updates_ui_state_and_can_be_changed() = runTest {
        val themeRepository = FakeThemePreferenceRepository()
        val viewModel = createViewModel(themeRepository = themeRepository)

        assertEquals(AppThemeModePreference.DEVICE, viewModel.uiState.value.themeModePreference)

        viewModel.setThemeModePreference(AppThemeModePreference.DARK)
        advanceUntilIdle()

        assertEquals(AppThemeModePreference.DARK, themeRepository.currentThemeModePreference)
        assertEquals(AppThemeModePreference.DARK, viewModel.uiState.value.themeModePreference)
    }

    @Test
    fun export_backup_updates_success_message() = runTest {
        var exportedDocumentId: String? = null
        val viewModel = createViewModel(
            exportBackupToDocumentUseCase = ExportBackupToDocumentUseCase(
                backupRepository = FakeBackupRepository(),
                backupDocumentRepository = FakeBackupDocumentRepository(onWrite = { documentId, _ ->
                    exportedDocumentId = documentId
                }),
                timeProvider = FakeTimeProvider(),
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
        val viewModel = createViewModel(
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
        val viewModel = createViewModel(
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

    private fun createViewModel(
        themeRepository: FakeThemePreferenceRepository = FakeThemePreferenceRepository(),
        exportBackupToDocumentUseCase: ExportBackupToDocumentUseCase = ExportBackupToDocumentUseCase(
            backupRepository = FakeBackupRepository(),
            backupDocumentRepository = FakeBackupDocumentRepository(),
            timeProvider = FakeTimeProvider(),
        ),
        restoreBackupFromDocumentUseCase: RestoreBackupFromDocumentUseCase = RestoreBackupFromDocumentUseCase(
            backupRepository = FakeBackupRepository(),
            backupDocumentRepository = FakeBackupDocumentRepository(),
        ),
    ): ConfigurationViewModel = ConfigurationViewModel(
        createBackupFileNameUseCase = CreateBackupFileNameUseCase(FakeTimeProvider()),
        observeThemeModePreferenceUseCase = ObserveThemeModePreferenceUseCase(themeRepository),
        setThemeModePreferenceUseCase = SetThemeModePreferenceUseCase(themeRepository),
        exportBackupToDocumentUseCase = exportBackupToDocumentUseCase,
        restoreBackupFromDocumentUseCase = restoreBackupFromDocumentUseCase,
    )

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

    private class FakeThemePreferenceRepository(
        initialThemeModePreference: AppThemeModePreference = AppThemeModePreference.DEVICE,
    ) : com.humans.aura.core.domain.interfaces.ThemePreferenceRepository {
        private val state = MutableStateFlow(initialThemeModePreference)

        val currentThemeModePreference: AppThemeModePreference
            get() = state.value

        override fun observeThemeModePreference(): Flow<AppThemeModePreference> = state

        override suspend fun setThemeModePreference(themeModePreference: AppThemeModePreference) {
            state.value = themeModePreference
        }
    }
}
