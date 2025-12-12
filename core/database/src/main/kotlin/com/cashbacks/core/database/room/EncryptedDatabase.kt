package com.cashbacks.core.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.cashbacks.core.database.room.entity.BankCardEntity
import com.cashbacks.core.database.room.entity.CashbackEntity
import com.cashbacks.core.database.room.entity.CategoryEntity
import com.cashbacks.core.database.room.entity.SettingsEntity
import com.cashbacks.core.database.room.entity.ShopEntity

@Database(
    entities = [
        BankCardEntity::class,
        CashbackEntity::class,
        ShopEntity::class,
        CategoryEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    PaymentSystemConverter::class,
    AmountConverter::class,
    MeasureUnitConverter::class,
    LocalDateConverter::class
)
abstract class EncryptedDatabase : AppDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: EncryptedDatabase? = null

        private const val DB_NAME = "EncryptedCashbacks.db"

        internal fun getDatabase(
            context: Context,
            factory: SupportSQLiteOpenHelper.Factory
        ): EncryptedDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(context, EncryptedDatabase::class.java, DB_NAME)
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}


fun EncryptedDatabase(context: Context, factory: SupportSQLiteOpenHelper.Factory): EncryptedDatabase {
    return EncryptedDatabase.getDatabase(context, factory)
}