package com.cashbacks.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cashbacks.data.model.BankCardDB
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.model.SettingsDB
import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.room.dao.CardsDao
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.data.room.dao.CategoriesDao
import com.cashbacks.data.room.dao.SettingsDao
import com.cashbacks.data.room.dao.ShopsDao

@Database(
    entities = [BankCardDB::class, CashbackDB::class, ShopDB::class, CategoryDB::class, SettingsDB::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardsDao(): CardsDao
    abstract fun shopsDao(): ShopsDao
    abstract fun cashbacksDao(): CashbacksDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tmpInstance = INSTANCE
            if (tmpInstance != null) {
                return tmpInstance
            }

            synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "SalesDatabase.db"
                    )
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}