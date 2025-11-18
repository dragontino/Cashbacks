package com.cashbacks.core.database.utils

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.cashbacks.core.database.AppDatabase

internal object DatabaseHelper {
    const val LEGACY_DB_NAME = "SalesDatabase.db"
    const val ENCRYPTED_DB_NAME = "EncryptedCashbacks.db"
    

    fun buildLegacyDatabase(context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context = context.applicationContext,
                klass = AppDatabase::class.java,
                name = LEGACY_DB_NAME
            )
            .build()
    }


    fun buildEncryptedDatabase(
        context: Context,
        factory: SupportSQLiteOpenHelper.Factory
    ): AppDatabase {
        return Room
            .databaseBuilder(
                context = context.applicationContext,
                klass = AppDatabase::class.java,
                name = ENCRYPTED_DB_NAME
            )
            .openHelperFactory(factory)
            .build()
    }
}