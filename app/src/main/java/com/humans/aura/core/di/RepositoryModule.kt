package com.humans.aura.core.di

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.ChatRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.features.day_summary.data.DaySummaryContextJsonEncoder
import com.humans.aura.features.day_summary.data.DaySummaryReflectionParser
import com.humans.aura.features.assistant_chat.data.RoomChatRepository
import com.humans.aura.features.day_summary.data.RoomConversationContextRepository
import com.humans.aura.features.day_summary.data.RoomDaySummaryRepository
import com.humans.aura.features.daily_goals.data.RoomDailyGoalRepository
import com.humans.aura.features.stopwatch.data.RoomActivityRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ActivityRepository> { RoomActivityRepository(get(), get(), get()) }
    single<DailyGoalRepository> { RoomDailyGoalRepository(get(), get()) }
    single { DaySummaryReflectionParser(get()) }
    single<DaySummaryRepository> { RoomDaySummaryRepository(get(), get(), get()) }
    single { DaySummaryContextJsonEncoder(get()) }
    single<ConversationContextRepository> { RoomConversationContextRepository(get(), get(), get(), get()) }
    single<ChatRepository> { RoomChatRepository(get(), get()) }
}
