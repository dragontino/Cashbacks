package com.cashbacks.domain.usecase.settings

import android.util.Log
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCase(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "SettingsUseCase"
    }

    suspend fun updateSettings(settings: Settings): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateSettings(settings).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }

    fun fetchSettings(onFailure: (Throwable) -> Unit = {}): Flow<Settings> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }
        return repository.fetchSettings().flowOn(handler + dispatcher)
    }
}