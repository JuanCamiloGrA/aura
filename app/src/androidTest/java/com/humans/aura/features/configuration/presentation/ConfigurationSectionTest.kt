package com.humans.aura.features.configuration.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import com.humans.aura.core.domain.models.AppThemeModePreference
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConfigurationSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_renders_theme_options_and_selects_oled_dark() {
        var selectedThemeMode = AppThemeModePreference.DEVICE

        composeRule.setContent {
            AuraTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ConfigurationSection(
                        uiState = ConfigurationUiState(
                            themeModePreference = selectedThemeMode,
                            suggestedBackupFileName = "aura-backup.aura",
                        ),
                        onThemeModeSelected = { selectedThemeMode = it },
                        onExportBackup = {},
                        onRestoreBackup = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag("configuration_theme_mode_card").assertIsDisplayed()
        composeRule.onNodeWithTag("configuration_theme_option_dark").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("configuration_theme_option_dark").performClick()

        assertEquals(AppThemeModePreference.DARK, selectedThemeMode)
    }
}
