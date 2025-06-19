package com.cashbacks.features.settings.domain.repo

import com.cashbacks.features.settings.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun updateSettings(settings: Settings): Result<Unit>

    fun fetchSettings(): Flow<Settings>

    suspend fun getSettings(): Result<Settings>
}