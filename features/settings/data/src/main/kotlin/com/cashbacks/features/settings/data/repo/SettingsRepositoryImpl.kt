package com.cashbacks.features.settings.data.repo

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.core.database.dao.SettingsDao
import com.cashbacks.core.database.utils.mapToDomainSettings
import com.cashbacks.core.database.utils.mapToEntity
import com.cashbacks.features.settings.data.resources.SaveSettingsException
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.repo.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

internal class SettingsRepositoryImpl(
    private val dao: SettingsDao,
    private val context: Context
) : SettingsRepository {

    private suspend fun insertSettings(settings: Settings): Result<Long> {
        val resultId = dao.insertSettings(settings.mapToEntity())
        return when {
            resultId < 0 -> Result.failure(SaveSettingsException.toException(context))
            else -> Result.success(resultId)
        }
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> {
        val count = dao.updateSettings(settings.mapToEntity())
        return when {
            count < 1 -> insertSettings(settings).map { Unit }
            else -> Result.success(Unit)
        }
    }

    override fun fetchSettings(): Flow<Settings> {
        return dao
            .fetchSettings()
            .distinctUntilChanged()
            .mapNotNull { entity ->
                if (entity == null) insertSettings(Settings())
                entity?.mapToDomainSettings()
            }
    }
}