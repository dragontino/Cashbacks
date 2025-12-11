package com.cashbacks.components.login.domain.usecase

import android.content.Context
import android.util.Log
import com.cashbacks.components.login.domain.model.LoginCredentials
import com.cashbacks.components.login.domain.repos.LoginRepository
import com.cashbacks.components.login.domain.util.validate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SignInUseCase {
    suspend operator fun invoke(credentials: LoginCredentials): Result<Unit>
}


internal class SignInUseCaseImpl(
    private val repository: LoginRepository,
    private val dispatcher: CoroutineDispatcher,
    private val context: Context
) : SignInUseCase {
    companion object {
        private const val TAG = "SignInUseCase"
    }

    override suspend fun invoke(credentials: LoginCredentials): Result<Unit> {
        val validationResult = credentials.validate(context)
        return if (validationResult.isFailure) {
            validationResult
        } else {
            withContext(dispatcher) {
                repository.signIn(credentials).onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }
}