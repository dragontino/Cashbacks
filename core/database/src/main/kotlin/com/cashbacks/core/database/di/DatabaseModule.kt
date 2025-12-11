package com.cashbacks.core.database.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.cashbacks.core.database.BuildConfig
import com.cashbacks.core.database.EncryptedDatabase
import com.cashbacks.core.database.LegacyDatabase
import com.cashbacks.core.database.datastore.CredentialsDataStore
import com.cashbacks.core.database.datastore.credentialsDataStore
import com.cashbacks.core.database.encryption.SqlCipherKeyManager
import com.cashbacks.core.database.source.CredentialsLocalDataSource
import com.cashbacks.core.database.source.CredentialsLocalDataSourceImpl
import com.cashbacks.core.database.utils.DatabaseMigrator
import com.cashbacks.core.database.utils.DatabaseMigratorImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DatabaseModule = module {
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

    single<CredentialsLocalDataSource> {
        CredentialsLocalDataSourceImpl(get())
    }

    single<CredentialsDataStore> {
        androidContext().credentialsDataStore
    }
}