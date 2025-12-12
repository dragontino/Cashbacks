package com.cashbacks.core.database.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.cashbacks.core.database.BuildConfig
import com.cashbacks.core.database.room.DatabaseMigrator
import com.cashbacks.core.database.room.DatabaseMigratorImpl
import com.cashbacks.core.database.room.EncryptedDatabase
import com.cashbacks.core.database.room.LegacyDatabase
import com.cashbacks.core.database.room.encryption.SqlCipherKeyManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val RoomModule = module {
    single {
        get<EncryptedDatabase>().settingsDao
    }

    single {
        get<EncryptedDatabase>().cardsDao
    }

    single {
        get<EncryptedDatabase>().cashbacksDao
    }

    single {
        get<EncryptedDatabase>().shopsDao
    }

    single {
        get<EncryptedDatabase>().categoriesDao
    }

    single<EncryptedDatabase> {
        EncryptedDatabase(androidContext(), get())
    }

    single<LegacyDatabase> {
        LegacyDatabase(androidContext())
    }

    single<SupportSQLiteOpenHelper.Factory>(createdAtStart = true) {
        when {
            BuildConfig.DEBUG -> FrameworkSQLiteOpenHelperFactory()
            else -> SqlCipherKeyManager(androidApplication()).getOpenHelperFactory()
        }
    }

    single<DatabaseMigrator> {
        DatabaseMigratorImpl(
            context = androidContext(),
            encryptedDatabase = get(),
            legacyDbProvider = { get() }
        )
    }
}