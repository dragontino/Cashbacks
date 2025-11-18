package com.cashbacks.core.database.utils

import android.content.Context
import androidx.core.content.edit
import com.cashbacks.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface DatabaseMigrator {
    fun needToMigrate(): Boolean

    suspend fun migrate(): Result<Unit>
}



internal class DatabaseMigratorImpl(
    context: Context,
    private val encryptedDatabase: AppDatabase,
    private val legacyDbProvider: () -> AppDatabase
) : DatabaseMigrator {
    private val context = context.applicationContext
    private val preferences = context.getSharedPreferences(MIGRATOR_PREFS, Context.MODE_PRIVATE)

    override fun needToMigrate(): Boolean {
        if (preferences.getBoolean(KEY_MIGRATION_DONE, false)) {
            return false
        }

        val legacyDbFile = context.getDatabasePath(DatabaseHelper.LEGACY_DB_NAME)
        return legacyDbFile.exists().also { exists ->
            if (!exists) {
                preferences.edit { putBoolean(KEY_MIGRATION_DONE, true) }
            }
        }
    }


    override suspend fun migrate(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val legacyDatabase = legacyDbProvider()

            encryptedDatabase.runInTransaction {
                launch { migrateCategoriesDao(legacyDatabase, encryptedDatabase) }
                launch { migrateShopsDao(legacyDatabase, encryptedDatabase) }
                launch { migrateCashbacksDao(legacyDatabase, encryptedDatabase) }
                launch { migrateCardsDao(legacyDatabase, encryptedDatabase) }
                launch { migrateSettingsDao(legacyDatabase, encryptedDatabase) }
            }
            legacyDatabase.close()
            context.deleteDatabase(DatabaseHelper.LEGACY_DB_NAME)
            preferences.edit {
                putBoolean(KEY_MIGRATION_DONE, true)
            }
        }
    }


    private suspend fun migrateCategoriesDao(
        legacyDb: AppDatabase,
        encryptedDb: AppDatabase
    ) {
        val categories = legacyDb.categoriesDao.getAll()
        encryptedDb.categoriesDao.insertAll(categories)
    }

    private suspend fun migrateShopsDao(legacyDb: AppDatabase, encryptedDb: AppDatabase) {
        val shops = legacyDb.shopsDao.getAll()
        encryptedDb.shopsDao.insertAll(shops)
    }

    private suspend fun migrateCashbacksDao(legacyDb: AppDatabase, encryptedDb: AppDatabase) {
        val cashbacks = legacyDb.cashbacksDao.getAll()
        encryptedDb.cashbacksDao.insertAll(cashbacks)
    }

    private suspend fun migrateCardsDao(legacyDb: AppDatabase, encryptedDb: AppDatabase) {
        val cards = legacyDb.cardsDao.getAll()
        encryptedDb.cardsDao.insertAll(cards)
    }

    private suspend fun migrateSettingsDao(legacyDb: AppDatabase, encryptedDb: AppDatabase) {
        legacyDb.settingsDao.getSettings()?.let {
            encryptedDb.settingsDao.insertSettings(it)
        }
    }


    private companion object {
        const val MIGRATOR_PREFS = "database_migrator"
        const val KEY_MIGRATION_DONE = "migration_is_done"
    }
}