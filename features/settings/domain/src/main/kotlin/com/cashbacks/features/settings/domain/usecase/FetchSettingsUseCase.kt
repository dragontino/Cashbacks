package com.cashbacks.features.settings.domain.usecase

import android.util.Log
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.domain.repo.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchSettingsUseCase {
    operator fun invoke(): Flow<Settings>
}


internal class FetchSettingsUseCaseImpl(
    private val repository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchSettingsUseCase {
    private companion object {
        const val TAG = "FetchSettingsUseCase"
    }

    override fun invoke(): Flow<Settings> {
        return repository
            .fetchSettings()
            .flowOn(dispatcher)
            .catch { throwable ->
                Log.e(TAG, throwable.message, throwable)
                throw throwable
            }
    }
}