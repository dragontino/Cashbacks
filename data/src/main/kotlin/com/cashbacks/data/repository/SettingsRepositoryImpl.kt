package com.cashbacks.data.repository

import com.cashbacks.data.model.SettingsDB
import com.cashbacks.data.room.dao.SettingsDao
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val dao: SettingsDao) : SettingsRepository {
    override suspend fun addSettings(settings: Settings): Long? {
        return dao.addSettings(SettingsDB(settings))
    }

    override suspend fun updateSettingsProperty(name: String, value: Any): Result<Long> {
        val updatedRowsCount = when (name) {
            "colorDesign" -> dao.updateColorDesign(value as String)
            "dynamicColor" -> dao.updateDynamicColor(value as Boolean)
            else -> null
        }

        return updatedRowsCount
            ?.let { Result.success(it.toLong()) }
            ?: Result.failure(Exception())
    }

    override fun fetchSettings(): Flow<Settings?> {
        return dao.fetchSettings().map { it?.mapToSettings() }
    }
}