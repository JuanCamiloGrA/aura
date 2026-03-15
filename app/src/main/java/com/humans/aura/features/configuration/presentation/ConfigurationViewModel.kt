package com.humans.aura.features.configuration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.features.configuration.domain.CreateBackupFileNameUseCase
import com.humans.aura.features.configuration.domain.ExportBackupToDocumentUseCase
import com.humans.aura.features.configuration.domain.RestoreBackupFromDocumentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val createBackupFileNameUseCase: CreateBackupFileNameUseCase,
    private val exportBackupToDocumentUseCase: ExportBackupToDocumentUseCase,
    private val restoreBackupFromDocumentUseCase: RestoreBackupFromDocumentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfigurationUiState(
            suggestedBackupFileName = createBackupFileNameUseCase(),
        ),
    )
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    fun refreshSuggestedBackupFileName(): String {
        val fileName = createBackupFileNameUseCase()
        _uiState.update { currentState ->
            currentState.copy(suggestedBackupFileName = fileName)
        }
        return fileName
    }

    fun exportBackup(documentId: String) {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isExporting = true,
                    statusMessage = null,
                    isStatusError = false,
                )
            }

            runCatching {
                exportBackupToDocumentUseCase(documentId)
            }.onSuccess {
                _uiState.update { currentState ->
                    currentState.copy(
                        isExporting = false,
                        statusMessage = "Backup saved as a .aura archive.",
                        isStatusError = false,
                        suggestedBackupFileName = createBackupFileNameUseCase(),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isExporting = false,
                        statusMessage = throwable.message ?: "AURA could not create the backup.",
                        isStatusError = true,
                    )
                }
            }
        }
    }

    fun restoreBackup(documentId: String) {
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isRestoring = true,
                    statusMessage = null,
                    isStatusError = false,
                )
            }

            runCatching {
                restoreBackupFromDocumentUseCase(documentId)
            }.onSuccess { summary ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isRestoring = false,
                        statusMessage = "Backup restored. Local data now matches the archive.",
                        isStatusError = false,
                        lastRestoredSummary = summary,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isRestoring = false,
                        statusMessage = throwable.message ?: "AURA could not restore the backup.",
                        isStatusError = true,
                    )
                }
            }
        }
    }
}
