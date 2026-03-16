package com.humans.aura.features.configuration.presentation

import com.humans.aura.core.domain.models.AppThemeModePreference
import com.humans.aura.core.domain.models.AuraBackupSummary

data class ConfigurationUiState(
    val suggestedBackupFileName: String = "aura-backup.aura",
    val themeModePreference: AppThemeModePreference = AppThemeModePreference.DEVICE,
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val statusMessage: String? = null,
    val isStatusError: Boolean = false,
    val lastRestoredSummary: AuraBackupSummary? = null,
) {
    val isBusy: Boolean = isExporting || isRestoring
}
