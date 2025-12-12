package com.cashbacks.core.database.room

import androidx.room.RoomDatabase
import com.cashbacks.core.database.room.dao.CardsDao
import com.cashbacks.core.database.room.dao.CashbacksDao
import com.cashbacks.core.database.room.dao.CategoriesDao
import com.cashbacks.core.database.room.dao.SettingsDao
import com.cashbacks.core.database.room.dao.ShopsDao

abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val categoriesDao: CategoriesDao
    abstract val shopsDao: ShopsDao
    abstract val cashbacksDao: CashbacksDao
    abstract val cardsDao: CardsDao
}