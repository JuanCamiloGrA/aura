package com.humans.aura.core.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humans.aura.features.assistant_chat.presentation.AssistantChatSection
import com.humans.aura.features.daily_goals.presentation.DailyGoalsSection
import com.humans.aura.features.day_summary.presentation.DaySummarySection
import com.humans.aura.features.stopwatch.presentation.StopwatchSection

private enum class AuraDestination(val label: String, val tag: String) {
    DASHBOARD("Dashboard", "nav_dashboard"),
    CHAT("Assistant", "nav_assistant"),
    SUMMARY("Summary", "nav_summary"),
}

@Composable
fun AuraApp(
    stopwatchSection: @Composable () -> Unit = { StopwatchSection() },
    dailyGoalsSection: @Composable () -> Unit = { DailyGoalsSection() },
    daySummarySection: @Composable () -> Unit = { DaySummarySection() },
    assistantChatSection: @Composable () -> Unit = { AssistantChatSection() },
) {
    var destination by remember { mutableStateOf(AuraDestination.DASHBOARD) }

    Scaffold(
        topBar = { AuraTopBar() },
        bottomBar = {
            AuraBottomBar(
                destination = destination,
                onNavigate = { destination = it },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            transitionSpec = {
                (fadeIn(spring(stiffness = 700f)) + scaleIn(initialScale = 0.98f))
                    .togetherWith(fadeOut(spring(stiffness = 900f)))
            },
            label = "tab_transition",
        ) { target ->
            when (target) {
                AuraDestination.DASHBOARD -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                    ) {
                        stopwatchSection()
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.5.dp,
                        )
                        dailyGoalsSection()
                        Spacer(Modifier.height(8.dp))
                    }
                }

                AuraDestination.CHAT -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        assistantChatSection()
                    }
                }

                AuraDestination.SUMMARY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        daySummarySection()
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun AuraTopBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = "AURA",
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 12.dp),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// ── Bottom Nav ──────────────────────────────────────────────────────────────

@Composable
private fun AuraBottomBar(
    destination: AuraDestination,
    onNavigate: (AuraDestination) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 0.5.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuraDestination.entries.forEach { dest ->
                    BottomNavItem(
                        label = dest.label,
                        selected = destination == dest,
                        testTag = dest.tag,
                        onClick = { onNavigate(dest) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(spring(stiffness = 600f)) + scaleIn(initialScale = 0f),
            exit = fadeOut(spring(stiffness = 900f)),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape),
            )
        }
    }
}
