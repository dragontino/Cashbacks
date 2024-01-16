package com.cashbacks.app.di

import android.app.Application
import com.cashbacks.data.repository.CashbackRepositoryImpl
import com.cashbacks.data.repository.CategoryRepositoryImpl
import com.cashbacks.data.repository.SettingsRepositoryImpl
import com.cashbacks.data.repository.ShopRepositoryImpl
import com.cashbacks.data.room.AppDatabase
import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.repository.CategoryRepository
import com.cashbacks.domain.repository.SettingsRepository
import com.cashbacks.domain.repository.ShopRepository
import com.cashbacks.domain.usecase.AddCategoryUseCase
import com.cashbacks.domain.usecase.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.CashbackShopUseCase
import com.cashbacks.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.EditCategoryUseCase
import com.cashbacks.domain.usecase.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.GetCashbackUseCase
import com.cashbacks.domain.usecase.GetShopUseCase
import com.cashbacks.domain.usecase.SettingsUseCase
import com.cashbacks.domain.usecase.ShopUseCase
import kotlinx.coroutines.Dispatchers

class DependencyFactory(private val application: Application) {
    fun provideSettingsUseCase() = SettingsUseCase(
        repository = provideSettingsRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideAddCategoryUseCase() = AddCategoryUseCase(
        repository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideFetchCategoriesUseCase() = FetchCategoriesUseCase(
        repository = provideCategoryRepository()
    )

    fun provideEditCategoryUseCase() = EditCategoryUseCase(
        categoryRepository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideDeleteCategoryUseCase() = DeleteCategoryUseCase(
        repository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideShopUseCase() = ShopUseCase(
        repository = provideShopRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideCashbackCategoryUseCase() = CashbackCategoryUseCase(
        repository = provideCashbackRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideCashbackShopUseCase() = CashbackShopUseCase(
        repository = provideCashbackRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideGetShopUseCase() = GetShopUseCase(
        repository = provideShopRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideGetCashbackUseCase() = GetCashbackUseCase(
        repository = provideCashbackRepository(),
        dispatcher = Dispatchers.IO
    )


    private fun provideSettingsRepository(): SettingsRepository {
        val database = provideDatabase()
        return SettingsRepositoryImpl(database.settingsDao())
    }

    private fun provideCategoryRepository(): CategoryRepository {
        val database = provideDatabase()
        return CategoryRepositoryImpl(database.categoriesDao())
    }

    private fun provideShopRepository(): ShopRepository {
        val database = provideDatabase()
        return ShopRepositoryImpl(database.shopsDao())
    }

    private fun provideCashbackRepository(): CashbackRepository {
        val database = provideDatabase()
        return CashbackRepositoryImpl(database.cashbacksDao())
    }

    private fun provideDatabase() = AppDatabase.getDatabase(application)
}