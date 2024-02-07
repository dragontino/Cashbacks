package com.cashbacks.app.di

import android.app.Application
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
import com.cashbacks.domain.usecase.card.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.card.EditBankCardUseCase
import com.cashbacks.domain.usecase.card.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.card.GetBankCardUseCase
import com.cashbacks.domain.usecase.cashback.CashbackCategoryUseCase
import com.cashbacks.domain.usecase.cashback.CashbackShopUseCase
import com.cashbacks.domain.usecase.cashback.EditCashbackUseCase
import com.cashbacks.domain.usecase.cashback.FetchCashbacksUseCase
import com.cashbacks.domain.usecase.categories.AddCategoryUseCase
import com.cashbacks.domain.usecase.categories.DeleteCategoryUseCase
import com.cashbacks.domain.usecase.categories.FetchCategoriesUseCase
import com.cashbacks.domain.usecase.categories.GetCategoryUseCase
import com.cashbacks.domain.usecase.categories.UpdateCategoryUseCase
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import com.cashbacks.domain.usecase.shops.AddShopUseCase
import com.cashbacks.domain.usecase.shops.DeleteShopUseCase
import com.cashbacks.domain.usecase.shops.EditShopUseCase
import com.cashbacks.domain.usecase.shops.FetchShopsFromCategoryUseCase
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

    fun provideAddShopUseCase() = AddShopUseCase(
        repository = provideShopRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideFetchCategoriesUseCase() = FetchCategoriesUseCase(
        repository = provideCategoryRepository()
    )

    fun provideUpdateCategoryUseCase() = UpdateCategoryUseCase(
        categoryRepository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideGetCategoryUseCase() = GetCategoryUseCase(
        repository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideDeleteCategoryUseCase() = DeleteCategoryUseCase(
        repository = provideCategoryRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideShopUseCase() = EditShopUseCase(
        repository = provideShopRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideDeleteShopUseCase() = DeleteShopUseCase(
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

    fun provideFetchShopsUseCase() = FetchShopsFromCategoryUseCase(
        repository = provideShopRepository()
    )

    fun provideEditCashbackUseCase() = EditCashbackUseCase(
        repository = provideCashbackRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideFetchCashbacksUseCase() = FetchCashbacksUseCase(
        repository = provideCashbackRepository()
    )

    fun provideFetchBankCardsUseCase() = FetchBankCardsUseCase(
        repository = provideBankCardRepository()
    )

    fun provideGetBankCardUseCase() = GetBankCardUseCase(
        repository = provideBankCardRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideEditBankCardUseCase() = EditBankCardUseCase(
        repository = provideBankCardRepository(),
        dispatcher = Dispatchers.IO
    )

    fun provideDeleteBankCardUseCase() = DeleteBankCardUseCase(
        repository = provideBankCardRepository(),
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

    private fun provideBankCardRepository(): BankCardRepository {
        val database = provideDatabase()
        return BankCardRepositoryImpl(database.cardsDao())
    }

    private fun provideDatabase() = AppDatabase.getDatabase(application)
}