package com.cashbacks.core.database

import androidx.room.RoomDatabase
import com.cashbacks.core.database.dao.CardsDao
import com.cashbacks.core.database.dao.CashbacksDao
import com.cashbacks.core.database.dao.CategoriesDao
import com.cashbacks.core.database.dao.SettingsDao
import com.cashbacks.core.database.dao.ShopsDao

abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val categoriesDao: CategoriesDao
    abstract val shopsDao: ShopsDao
    abstract val cashbacksDao: CashbacksDao
    abstract val cardsDao: CardsDao
}