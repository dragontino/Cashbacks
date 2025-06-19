package com.cashbacks.features.settings.domain.usecase

import android.util.Log
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.repo.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetSettingsUseCase {
    suspend operator fun invoke(): Result<Settings>
}


internal class GetSettingsUseCaseImpl(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) : GetSettingsUseCase {
    private companion object {
        const val TAG = "GetSettingsUseCase"
    }

    override suspend fun invoke(): Result<Settings> {
        return withContext(dispatcher) {
            repository.getSettings().onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}