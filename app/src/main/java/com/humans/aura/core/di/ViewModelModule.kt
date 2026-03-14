package com.humans.aura.core.di

import com.humans.aura.features.assistant_chat.presentation.AssistantChatViewModel
import com.humans.aura.features.day_summary.presentation.DaySummaryViewModel
import com.humans.aura.features.daily_goals.presentation.DailyGoalsViewModel
import com.humans.aura.features.stopwatch.presentation.StopwatchViewModel
import com.humans.aura.features.voice.presentation.VoiceViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::StopwatchViewModel)
    viewModelOf(::DailyGoalsViewModel)
    viewModelOf(::DaySummaryViewModel)
    viewModelOf(::AssistantChatViewModel)
    viewModelOf(::VoiceViewModel)
}
