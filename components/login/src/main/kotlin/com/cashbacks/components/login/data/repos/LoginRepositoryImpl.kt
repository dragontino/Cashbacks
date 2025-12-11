package com.cashbacks.components.login.data.repos

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.components.login.data.util.NoSavedCredentialsException
import com.cashbacks.components.login.data.util.WrongPinException
import com.cashbacks.components.login.domain.model.LoginCredentials
import com.cashbacks.components.login.domain.repos.LoginRepository
import com.cashbacks.core.database.datastore.model.LoginCredentialsDto
import com.cashbacks.core.database.source.CredentialsLocalDataSource

internal class LoginRepositoryImpl(
    private val credentialsLocalDataSource: CredentialsLocalDataSource,
    private val context: Context
) : LoginRepository {
    override suspend fun signUp(credentials: LoginCredentials): Result<Unit> = runCatching {
        credentialsLocalDataSource.saveCredentials(credentials.mapToDto())
    }

    override suspend fun signIn(credentials: LoginCredentials): Result<Unit> = runCatching {
        val savedCredentials = credentialsLocalDataSource.getSavedCredentials()
            ?: throw NoSavedCredentialsException().toException(context)
        if (savedCredentials.mapToDomain() != credentials) {
            throw WrongPinException().toException(context)
        }
    }

    override suspend fun hasSavedCredentials(): Result<Boolean> = runCatching {
        credentialsLocalDataSource.getSavedCredentials() != null
    }

    private fun LoginCredentials.mapToDto() = LoginCredentialsDto(password = password)
    private fun LoginCredentialsDto.mapToDomain() = LoginCredentials(password = password)
}