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

    override fun fetchSettings(): Flow<Settings> {
        return dao.fetchSettings().map { it?.mapToDomainSettings() ?: Settings() }
    }
}