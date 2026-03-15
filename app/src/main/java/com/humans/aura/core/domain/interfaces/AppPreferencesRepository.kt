package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AppPreferencesSnapshot

interface AppPreferencesRepository {
    suspend fun snapshot(): AppPreferencesSnapshot

    suspend fun restore(snapshot: AppPreferencesSnapshot)
}
