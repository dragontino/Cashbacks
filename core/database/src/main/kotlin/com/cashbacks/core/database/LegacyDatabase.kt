package com.cashbacks.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.TypeConverters
import com.cashbacks.core.database.entity.BankCardEntity
import com.cashbacks.core.database.entity.CashbackEntity
import com.cashbacks.core.database.entity.CategoryEntity
import com.cashbacks.core.database.entity.SettingsEntity
import com.cashbacks.core.database.entity.ShopEntity
import java.io.File

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
internal abstract class LegacyDatabase : AppDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: LegacyDatabase? = null

        private const val DB_NAME = "SalesDatabase.db"

        internal fun getDatabase(context: Context): LegacyDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }

            synchronized(this) {
                val instance = Room
                    .databaseBuilder(context, LegacyDatabase::class.java, DB_NAME)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        internal fun dropDatabase(context: Context) {
            context.deleteDatabase(DB_NAME)
        }

        internal fun getDatabaseFile(context: Context): File {
            return context.getDatabasePath(DB_NAME)
        }
    }
}


internal fun LegacyDatabase(context: Context): LegacyDatabase {
    return LegacyDatabase.getDatabase(context)
}