package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun updateSettingsProperty(name: String, value: Any): Result<Long>

    suspend fun updateSettings(settings: Settings): Result<Unit>

    fun fetchSettings(): Flow<Settings>
}