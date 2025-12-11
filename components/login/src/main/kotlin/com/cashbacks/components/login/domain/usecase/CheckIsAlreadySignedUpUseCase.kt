package com.cashbacks.components.login.domain.usecase

import android.util.Log
import com.cashbacks.components.login.domain.repos.LoginRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface CheckIsAlreadySignedUpUseCase {
    suspend operator fun invoke(): Result<Boolean>
}


internal class CheckIsAlreadySignedUpUseCaseImpl(
    private val repository: LoginRepository,
    private val dispatcher: CoroutineDispatcher
) : CheckIsAlreadySignedUpUseCase {
    companion object {
        private const val TAG = "CheckIsAlreadySignedUpUseCase"
    }

    override suspend fun invoke(): Result<Boolean> {
        return withContext(dispatcher) {
            repository.hasSavedCredentials().onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}