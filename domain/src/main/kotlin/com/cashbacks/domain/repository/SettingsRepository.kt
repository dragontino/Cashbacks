package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun addSettings(settings: Settings): Long?

    suspend fun updateSettingsProperty(name: String, value: Any): Result<Long>

    fun fetchSettings(): Flow<Settings?>
}