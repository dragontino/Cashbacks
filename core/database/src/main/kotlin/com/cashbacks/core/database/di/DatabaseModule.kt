package com.cashbacks.core.database.di

import com.cashbacks.core.database.AppDatabase
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
        AppDatabase.getDatabase(androidContext())
    }
}