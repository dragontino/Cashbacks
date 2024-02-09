package com.cashbacks.app.di.modules

import android.content.Context
import com.cashbacks.data.repository.BankCardRepositoryImpl
import com.cashbacks.data.repository.CashbackRepositoryImpl
import com.cashbacks.data.repository.CategoryRepositoryImpl
import com.cashbacks.data.repository.SettingsRepositoryImpl
import com.cashbacks.data.repository.ShopRepositoryImpl
import com.cashbacks.data.room.AppDatabase
import com.cashbacks.domain.repository.BankCardRepository
import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.repository.CategoryRepository
import com.cashbacks.domain.repository.SettingsRepository
import com.cashbacks.domain.repository.ShopRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideSettingsRepository(database: AppDatabase): SettingsRepository {
        return SettingsRepositoryImpl(database.settingsDao())
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(database: AppDatabase): CategoryRepository {
        return CategoryRepositoryImpl(database.categoriesDao())
    }

    @Provides
    @Singleton
    fun provideShopRepository(database: AppDatabase): ShopRepository {
        return ShopRepositoryImpl(database.shopsDao())
    }

    @Provides
    @Singleton
    fun provideCashbackRepository(database: AppDatabase): CashbackRepository {
        return CashbackRepositoryImpl(database.cashbacksDao())
    }

    @Provides
    @Singleton
    fun provideBankCardRepository(database: AppDatabase): BankCardRepository {
        return BankCardRepositoryImpl(database.cardsDao())
    }

    @Provides
    @Singleton
    fun provideDatabase(): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}