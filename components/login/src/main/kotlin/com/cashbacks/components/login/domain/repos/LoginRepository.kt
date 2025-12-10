package com.cashbacks.components.login.domain.repos

internal interface LoginRepository {
    suspend fun signUp(password: String): Result<Unit>

    suspend fun signIn(password: String): Result<Unit>
}