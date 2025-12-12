package com.cashbacks.core.database.di

import com.cashbacks.core.database.datastore.CredentialsDataStore
import com.cashbacks.core.database.datastore.credentialsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val DataStoreModule = module {
    single<CredentialsDataStore> {
        androidContext().credentialsDataStore
    }
}