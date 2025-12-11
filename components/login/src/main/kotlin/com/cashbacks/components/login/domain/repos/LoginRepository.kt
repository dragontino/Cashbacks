package com.cashbacks.components.login.domain.repos

import com.cashbacks.components.login.domain.model.LoginCredentials

internal interface LoginRepository {
    suspend fun signUp(credentials: LoginCredentials): Result<Unit>

    suspend fun signIn(credentials: LoginCredentials): Result<Unit>

    suspend fun hasSavedCredentials(): Result<Boolean>
}