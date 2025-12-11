package com.cashbacks.core.database.source

import com.cashbacks.core.database.datastore.CredentialsDataStore
import com.cashbacks.core.database.datastore.model.LoginCredentialsDto
import kotlinx.coroutines.flow.firstOrNull

interface CredentialsLocalDataSource {
    suspend fun getSavedCredentials(): LoginCredentialsDto?

    suspend fun saveCredentials(newCredentials: LoginCredentialsDto)
}

internal class CredentialsLocalDataSourceImpl(
    private val dataStore: CredentialsDataStore
) : CredentialsLocalDataSource {
    override suspend fun getSavedCredentials(): LoginCredentialsDto? {
        // TODO: добавить ассиметричное шифрование
        return dataStore.data.firstOrNull()
    }

    override suspend fun saveCredentials(newCredentials: LoginCredentialsDto) {
        dataStore.updateData { old ->
            if (old == newCredentials) old else newCredentials
        }
    }
}