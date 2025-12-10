package com.cashbacks.components.login.domain.usecase

import android.util.Log
import com.cashbacks.components.login.domain.repos.LoginRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SignUpUseCase {
    suspend operator fun invoke(password: String): Result<Unit>
}


internal class SignUpUseCaseImpl(
    private val repository: LoginRepository,
    private val dispatcher: CoroutineDispatcher
) : SignUpUseCase {
    companion object {
        private const val TAG = "SignUpUseCase"
    }

    override suspend fun invoke(password: String): Result<Unit> {
        return withContext(dispatcher) {
            repository.signUp(password).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}