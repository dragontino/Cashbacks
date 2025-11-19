package com.cashbacks.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cashbacks.core.database.encryption.SqlCipherKeyManager
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.CategoryEntity
import com.cashbacks.core.database.utils.DatabaseMigrator
import com.cashbacks.core.database.utils.DatabaseMigratorImpl
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class DatabaseMigratorInstrumentedTest {
    private lateinit var context: Context
    private lateinit var encryptedDb: EncryptedDatabase
    private lateinit var legacyDb: LegacyDatabase
    private lateinit var migrator: DatabaseMigrator
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        val supportFactory = SqlCipherKeyManager(context).getSupportFactory()
        encryptedDb = Room.inMemoryDatabaseBuilder(context, EncryptedDatabase::class.java)
            .openHelperFactory(supportFactory)
            .build()
        legacyDb = LegacyDatabase(context)

        migrator = DatabaseMigratorImpl(context, encryptedDb, legacyDbProvider = { legacyDb })

        runBlocking {
            launch {
                val categories = List(3) { index ->
                    mockk<CategoryEntity>(relaxed = true).also { entity ->
                        every { entity.name } returns "Category #${index + 1}"
                    }
                }
                legacyDb.categoriesDao.insertAll(categories)
            }
            launch {
                val cards = List(2) {
                    mockk<BankCardEntity>(relaxed = true).also { entity ->
                        every { entity.paymentSystem } returns PaymentSystem.Visa
                    }
                }
                legacyDb.cardsDao.insertAll(cards)
            }
            launch {
                legacyDb.settingsDao.insertSettings(mockk(relaxed = true))
            }
        }
    }

    @Test
    fun testDatabaseMigration() {
        assertTrue(migrator.needToMigrate())

        runBlocking {
            suspend fun getData(db: AppDatabase) = with(db) {
                listOf(
                    async(Dispatchers.IO) { categoriesDao.getAll() },
                    async(Dispatchers.IO) { shopsDao.getAll() },
                    async(Dispatchers.IO) { cashbacksDao.getAll() },
                    async(Dispatchers.IO) { cardsDao.getAll() }
                ).awaitAll()
            }

            val (legacyCategories, legacyShops, legacyCashbacks, legacyCards) = getData(legacyDb)
            val oldSettings = legacyDb.settingsDao.getSettings()

            val result = withTimeout(10.seconds) {
                migrator.migrate().onFailure { it.printStackTrace() }
            }
            assertTrue(result.isSuccess)
            assertFalse(migrator.needToMigrate())

            val (newCategories, newShops, newCashbacks, newCards) = getData(encryptedDb)

            assertEquals(legacyCategories.size, newCategories.size)
            assertEquals(legacyShops.size, newShops.size)
            assertEquals(legacyCashbacks.size, newCashbacks.size)
            assertEquals(legacyCards.size, newCards.size)

            assertEquals(oldSettings, encryptedDb.settingsDao.getSettings())
        }
    }

    @After
    fun tearDown() {
        encryptedDb.close()
        legacyDb.close()
    }
}