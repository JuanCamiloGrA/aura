package com.humans.aura.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.humans.aura.features.daily_goals.presentation.DailyGoalsSection
import com.humans.aura.features.stopwatch.presentation.StopwatchSection

@Composable
fun AuraApp(
    stopwatchSection: @Composable () -> Unit = { StopwatchSection() },
    dailyGoalsSection: @Composable () -> Unit = { DailyGoalsSection() },
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AppHeader()
            FoundationStrip()
            stopwatchSection()
            dailyGoalsSection()
        }
    }
}

@Composable
private fun AppHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "AURA",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Milestone 1: unstoppable activity logging, local prediction, honest status tracking, and daily goals.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FoundationStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FoundationPill(text = "Compose")
        FoundationPill(text = "Room")
        FoundationPill(text = "Koin")
    }
}

@Composable
private fun FoundationPill(text: String) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
