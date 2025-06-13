package com.cashbacks.features.cashback.domain.di

import com.cashbacks.features.cashback.domain.usecase.AddCashbackToCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToCategoryUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToShopUseCase
import com.cashbacks.features.cashback.domain.usecase.AddCashbackToShopUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.DeleteCashbackUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.DeleteExpiredCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.DeleteExpiredCashbacksUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.FetchAllCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchAllCashbacksUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromCategoryUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromShopUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchCashbacksFromShopUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.FetchMaxCashbacksFromCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchMaxCashbacksFromCategoryUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.FetchMaxCashbacksFromShopUseCase
import com.cashbacks.features.cashback.domain.usecase.FetchMaxCashbacksFromShopUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.GetCashbackUseCase
import com.cashbacks.features.cashback.domain.usecase.GetCashbackUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromCategoryUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromShopUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMaxCashbacksFromShopUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.GetMeasureUnitsUseCase
import com.cashbacks.features.cashback.domain.usecase.GetMeasureUnitsUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.SearchCashbacksUseCase
import com.cashbacks.features.cashback.domain.usecase.SearchCashbacksUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInCategoryUseCase
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInCategoryUseCaseImpl
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInShopUseCase
import com.cashbacks.features.cashback.domain.usecase.UpdateCashbackInShopUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val CashbackDomainModule = module {
    single<DeleteCashbackUseCase> {
        DeleteCashbackUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<AddCashbackToCategoryUseCase> {
        AddCashbackToCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<AddCashbackToShopUseCase> {
        AddCashbackToShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<UpdateCashbackInCategoryUseCase> {
        UpdateCashbackInCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<UpdateCashbackInShopUseCase> {
        UpdateCashbackInShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetCashbackUseCase> {
        GetCashbackUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchAllCashbacksUseCase> {
        FetchAllCashbacksUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchCashbacksFromCategoryUseCase> {
        FetchCashbacksFromCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchCashbacksFromShopUseCase> {
        FetchCashbacksFromShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchMaxCashbacksFromCategoryUseCase> {
        FetchMaxCashbacksFromCategoryUseCaseImpl(get())
    }

    single<FetchMaxCashbacksFromShopUseCase> {
        FetchMaxCashbacksFromShopUseCaseImpl(get())
    }

    single<GetMaxCashbacksFromCategoryUseCase> {
        GetMaxCashbacksFromCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetMaxCashbacksFromShopUseCase> {
        GetMaxCashbacksFromShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<SearchCashbacksUseCase> {
        SearchCashbacksUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<DeleteExpiredCashbacksUseCase> {
        DeleteExpiredCashbacksUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetMeasureUnitsUseCase> {
        GetMeasureUnitsUseCaseImpl(Dispatchers.Default)
    }
}