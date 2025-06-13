package com.cashbacks.features.settings.domain.usecase

import android.util.Log
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.repo.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateSettingsUseCase {
    suspend operator fun invoke(settings: Settings): Result<Unit>
}


internal class UpdateSettingsUseCaseImpl(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateSettingsUseCase {
    private companion object {
        const val TAG = "UpdateSettingsUseCase"
    }

    override suspend fun invoke(settings: Settings): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateSettings(settings).onFailure { throwable ->
                Log.e(TAG, throwable.message, throwable)
            }
        }
    }
}