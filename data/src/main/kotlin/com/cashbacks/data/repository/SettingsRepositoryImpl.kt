package com.cashbacks.data.repository

import com.cashbacks.data.model.SettingsDB
import com.cashbacks.data.room.dao.SettingsDao
import com.cashbacks.domain.model.SaveSettingsException
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val dao: SettingsDao) : SettingsRepository {
    private suspend fun addSettings(settings: Settings): Result<Unit> {
        val count = dao.addSettings(SettingsDB(settings))
        return when {
            count == null || count < 1 -> Result.failure(SaveSettingsException)
            else -> Result.success(Unit)
        }
    }

    private suspend fun getNumberOfEntriesInDatabase(): Int {
        return dao.getRowCount()
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> {
        if (getNumberOfEntriesInDatabase() == 0) {
            return addSettings(settings)
        }

        val updatedRowsCount = dao.updateSettings(SettingsDB(settings))
        return when {
            updatedRowsCount < 1 -> Result.failure(SaveSettingsException)
            else -> Result.success(Unit)
        }
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

    override fun fetchSettings(): Flow<Settings> {
        return dao.fetchSettings().map { it?.mapToDomainSettings() ?: Settings() }
    }
}