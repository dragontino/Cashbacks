package com.cashbacks.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.cashbacks.core.database.dao.CardsDao
import com.cashbacks.core.database.dao.CashbacksDao
import com.cashbacks.core.database.dao.CategoriesDao
import com.cashbacks.core.database.dao.SettingsDao
import com.cashbacks.core.database.dao.ShopsDao
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.CashbackEntity
import com.cashbacks.core.database.entity.CategoryEntity
import com.cashbacks.core.database.entity.SettingsEntity
import com.cashbacks.core.database.entity.ShopEntity
import com.cashbacks.core.database.utils.DatabaseHelper

@Database(
    entities = [
        BankCardEntity::class,
        CashbackEntity::class,
        ShopEntity::class,
        CategoryEntity::class,
        SettingsEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(
    PaymentSystemConverter::class,
    AmountConverter::class,
    MeasureUnitConverter::class,
    LocalDateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val categoriesDao: CategoriesDao
    abstract val shopsDao: ShopsDao
    abstract val cashbacksDao: CashbacksDao
    abstract val cardsDao: CardsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        internal fun getDatabase(context: Context, factory: SupportSQLiteOpenHelper.Factory): AppDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }

            synchronized(this) {
                val instance = DatabaseHelper.buildEncryptedDatabase(context, factory)
                INSTANCE = instance
                return instance
            }
        }
    }
}


fun AppDatabase(context: Context, factory: SupportSQLiteOpenHelper.Factory): AppDatabase {
    return AppDatabase.getDatabase(context, factory)
}