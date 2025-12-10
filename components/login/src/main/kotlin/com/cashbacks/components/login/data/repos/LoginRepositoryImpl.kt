package com.cashbacks.components.login.data.repos

import com.cashbacks.components.login.domain.repos.LoginRepository

internal class LoginRepositoryImpl : LoginRepository {
    override suspend fun signUp(password: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun signIn(password: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}