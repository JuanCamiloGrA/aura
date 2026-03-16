package com.humans.aura.features.configuration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.presentation.theme.AuraTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfigurationThemeHost(
    viewModel: ConfigurationViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuraTheme(themeModePreference = uiState.themeModePreference) {
        content()
    }
}
