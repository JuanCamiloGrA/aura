package com.humans.aura.core.di

import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayActivitiesUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SaveTodayGoalUseCase
import com.humans.aura.features.day_summary.domain.AssembleDaySummaryContextUseCase
import com.humans.aura.features.day_summary.domain.BuildDaySummaryPromptUseCase
import com.humans.aura.features.day_summary.domain.CreatePendingDaySummaryUseCase
import com.humans.aura.features.day_summary.domain.GeneratePendingDaySummariesUseCase
import com.humans.aura.features.day_summary.domain.ObserveLatestSummaryUseCase
import com.humans.aura.features.day_summary.domain.ObserveRecentSummariesUseCase
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import com.humans.aura.features.assistant_chat.domain.BuildChatPromptUseCase
import com.humans.aura.features.assistant_chat.domain.EnsureChatSessionUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatMessagesUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatSessionsUseCase
import com.humans.aura.features.assistant_chat.domain.SendChatMessageUseCase
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import com.humans.aura.features.voice.domain.SpeakAssistantReplyUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveCurrentActivityUseCase(get()) }
    factory { ObserveRecentActivitiesUseCase(get()) }
    factory { LogNewActivityUseCase(get(), get()) }
    factory { PredictNextActivityTitleUseCase(get(), get()) }
    factory { UpdateCurrentActivityStatusUseCase(get()) }
    factory { ClearActivitiesUseCase(get()) }
    factory { ObserveTodayGoalUseCase(get()) }
    factory { ObserveTodayActivitiesUseCase(get(), get()) }
    factory { SaveTodayGoalUseCase(get()) }
    factory { ClearTodayGoalUseCase(get()) }
    factory { HandleSleepIntentUseCase(get(), get(), get(), get()) }
    factory { CreatePendingDaySummaryUseCase(get(), get()) }
    factory { AssembleDaySummaryContextUseCase(get(), get()) }
    factory { BuildDaySummaryPromptUseCase(get()) }
    factory { GeneratePendingDaySummariesUseCase(get(), get(), get(), get(), get()) }
    factory { ObserveLatestSummaryUseCase(get()) }
    factory { ObserveRecentSummariesUseCase(get()) }
    factory { BuildChatPromptUseCase() }
    factory { EnsureChatSessionUseCase(get()) }
    factory { ObserveChatMessagesUseCase(get()) }
    factory { ObserveChatSessionsUseCase(get()) }
    factory { SendChatMessageUseCase(get(), get(), get(), get()) }
    factory { NormalizeTranscriptToEnglishUseCase(get()) }
    factory { SpeakAssistantReplyUseCase(get()) }
}
