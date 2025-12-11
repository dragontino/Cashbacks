package com.cashbacks.core.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.cashbacks.core.database.datastore.model.LoginCredentialsDto
import com.cashbacks.core.database.datastore.model.LoginCredentialsSerializer

internal typealias CredentialsDataStore = DataStore<LoginCredentialsDto>

internal val Context.credentialsDataStore by dataStore(
    fileName = "credentials.json",
    serializer = LoginCredentialsSerializer
)
