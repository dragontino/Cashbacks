package com.cashbacks.core.database.di

import com.cashbacks.core.database.source.CategoryLocalDataSource
import com.cashbacks.core.database.source.CategoryLocalDataSourceImpl
import com.cashbacks.core.database.source.CredentialsLocalDataSource
import com.cashbacks.core.database.source.CredentialsLocalDataSourceImpl
import org.koin.dsl.module

val DatabaseModule = module {
    includes(DataStoreModule, RoomModule)

    single<CredentialsLocalDataSource> {
        CredentialsLocalDataSourceImpl(get())
    }

    single<CategoryLocalDataSource> {
        CategoryLocalDataSourceImpl(get())
    }
}