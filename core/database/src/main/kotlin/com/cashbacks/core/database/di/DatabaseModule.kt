package com.cashbacks.core.database.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.cashbacks.core.database.AppDatabase
import com.cashbacks.core.database.encryption.SqlCipherKeyManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DatabaseModule = module {
    single {
        get<AppDatabase>().settingsDao
    }

    single {
        get<AppDatabase>().cardsDao
    }

    single {
        get<AppDatabase>().cashbacksDao
    }

    single {
        get<AppDatabase>().shopsDao
    }

    single {
        get<AppDatabase>().categoriesDao
    }

    single {
        AppDatabase(androidContext(), get())
    }

    single<SupportSQLiteOpenHelper.Factory> {
        SqlCipherKeyManager(androidApplication()).getSupportFactory()
    }
}