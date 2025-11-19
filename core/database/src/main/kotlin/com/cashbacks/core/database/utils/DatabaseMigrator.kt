package com.cashbacks.core.database.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.room.withTransaction
import com.cashbacks.core.database.EncryptedDatabase
import com.cashbacks.core.database.LegacyDatabase
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.CashbackEntity
import com.cashbacks.core.database.entity.CategoryEntity
import com.cashbacks.core.database.entity.SettingsEntity
import com.cashbacks.core.database.entity.ShopEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

interface DatabaseMigrator {
    fun needToMigrate(): Boolean

    suspend fun migrate(): Result<Unit>
}



internal class DatabaseMigratorImpl(
    context: Context,
    private val encryptedDatabase: EncryptedDatabase,
    private val legacyDbProvider: () -> LegacyDatabase
) : DatabaseMigrator {
    private val context = context.applicationContext
    private val preferences = context.getSharedPreferences(MIGRATOR_PREFS, Context.MODE_PRIVATE)

    override fun needToMigrate(): Boolean {
        Log.d(TAG, "Checking if db migration is necessary")
        val isMigrationDone = preferences.getBoolean(KEY_MIGRATION_DONE, false)
        if (isMigrationDone) {
            Log.d(TAG, "Migration is already done")
            return false
        }

        val legacyDbFile = LegacyDatabase.getDatabaseFile(context)
        return legacyDbFile.exists()
            .also { exists ->
                if (!exists) {
                    preferences.edit { putBoolean(KEY_MIGRATION_DONE, true) }
                }
            }
            .also { Log.d(TAG, "Does legacy db file exist? $it") }
    }


    override suspend fun migrate(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val legacyDatabase = legacyDbProvider()
            Log.d(TAG, "Start extracting...")
            val categoriesDeferred = async { legacyDatabase.categoriesDao.getAll() }
            val shopsDeferred = async { legacyDatabase.shopsDao.getAll() }
            val cashbacksDeferred = async { legacyDatabase.cashbacksDao.getAll() }
            val cardsDeferred = async { legacyDatabase.cardsDao.getAll() }
            val settingsDeferred = async { legacyDatabase.settingsDao.getSettings() }

            Log.d(TAG, "Awaiting results...")
            val result = encryptedDatabase.withTransaction {
                var tmp = encryptedDatabase.insertCategories(categoriesDeferred.await()) +
                        encryptedDatabase.insertShops(shopsDeferred.await()) +
                        encryptedDatabase.insertCashbacks(cashbacksDeferred.await()) +
                        encryptedDatabase.insertCards(cardsDeferred.await())

                settingsDeferred.await()?.let {
                    tmp += encryptedDatabase.insertSettings(it)
                }
                tmp
            }
            Log.d(TAG, "The final result is $result")

            result.onFailure { throw it }

            legacyDatabase.close()
            LegacyDatabase.dropDatabase(context)
            Log.d(TAG, "Legacy database is successfully closed and deleted")
            preferences.edit {
                putBoolean(KEY_MIGRATION_DONE, true)
            }
        }
    }


    private suspend fun EncryptedDatabase.insertCategories(
        categories: List<CategoryEntity>
    ) = runCatching {
        val newIds = categoriesDao.insertAll(categories)
        if (categories.size != newIds.size) {
            throwRuntimeException("categories", initialCount = categories.size, addedCount = newIds.size)
        }
    }

    private suspend fun EncryptedDatabase.insertShops(shops: List<ShopEntity>) = runCatching {
        val newIds = shopsDao.insertAll(shops)
        if (shops.size != newIds.size) {
            throwRuntimeException("shops", initialCount = shops.size, addedCount = newIds.size)
        }
    }

    private suspend fun EncryptedDatabase.insertCashbacks(
        cashbacks: List<CashbackEntity>
    ) = runCatching {
        val newIds = cashbacksDao.insertAll(cashbacks)
        if (cashbacks.size != newIds.size) {
            throwRuntimeException("cashbacks", cashbacks.size, newIds.size)
        }
    }

    private suspend fun EncryptedDatabase.insertCards(
        cards: List<BankCardEntity>
    ) = runCatching {
        val newIds = cardsDao.insertAll(cards)
        if (cards.size != newIds.size) {
            throwRuntimeException("bankCards", cards.size, newIds.size)
        }
    }

    private suspend fun EncryptedDatabase.insertSettings(
        settings: SettingsEntity
    ): Result<Unit> = runCatching {
        settingsDao.insertSettings(settings)
    }

    private operator fun Result<*>.plus(other: Result<*>): Result<Unit> {
        if (this.isSuccess && other.isSuccess) {
            return Result.success(Unit)
        } else {
            val msg = buildString {
                this@plus.exceptionOrNull()?.localizedMessage?.let(::appendLine)
                other.exceptionOrNull()?.localizedMessage?.let(::appendLine)
            }.trim()
            return Result.failure(RuntimeException(msg))
        }
    }


    private companion object {
        fun throwRuntimeException(name: String, initialCount: Int, addedCount: Int): Exception {
            throw RuntimeException(
                "Failed to migrate $name. There were $initialCount $name in legacy db but $addedCount $name were added to the encrypted db"
            )
        }
        const val MIGRATOR_PREFS = "database_migrator"
        const val KEY_MIGRATION_DONE = "migration_is_done"

        const val TAG = "DatabaseMigrator"
    }
}