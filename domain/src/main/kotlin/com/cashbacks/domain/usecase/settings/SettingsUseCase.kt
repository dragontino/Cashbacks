package com.cashbacks.domain.usecase.settings

import android.util.Log
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCase(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "SettingsUseCase"
    }

    suspend fun updateSettingsProperty(name: String, value: Any): Result<Long> {
        return withContext(dispatcher) {
            val result = repository.updateSettingsProperty(name, value)
            result.exceptionOrNull()?.let { exception ->
                Log.e(TAG, exception.message, exception)
            }
            return@withContext result
        }
    }

    fun fetchSettings(): Flow<Settings> = repository.fetchSettings().mapLatest { settings ->
        when (settings) {
            null -> {
                repository.addSettings(Settings())
                delay(200)
                return@mapLatest Settings()
            }
            else -> return@mapLatest settings
        }
    }
}