package com.cashbacks.features.shop.domain.di

import com.cashbacks.features.shop.domain.usecase.AddShopUseCase
import com.cashbacks.features.shop.domain.usecase.AddShopUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCase
import com.cashbacks.features.shop.domain.usecase.DeleteShopUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.FetchAllShopsUseCase
import com.cashbacks.features.shop.domain.usecase.FetchAllShopsUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.FetchShopsFromCategoryUseCase
import com.cashbacks.features.shop.domain.usecase.FetchShopsFromCategoryUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.FetchShopsWithCashbackUseCase
import com.cashbacks.features.shop.domain.usecase.FetchShopsWithCashbackUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.GetShopUseCase
import com.cashbacks.features.shop.domain.usecase.GetShopUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.SearchShopsUseCase
import com.cashbacks.features.shop.domain.usecase.SearchShopsUseCaseImpl
import com.cashbacks.features.shop.domain.usecase.UpdateShopUseCase
import com.cashbacks.features.shop.domain.usecase.UpdateShopUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val ShopDomainModule = module {
    single<AddShopUseCase> {
        AddShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<UpdateShopUseCase> {
        UpdateShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<DeleteShopUseCase> {
        DeleteShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetShopUseCase> {
        GetShopUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchShopsFromCategoryUseCase> {
        FetchShopsFromCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchAllShopsUseCase> {
        FetchAllShopsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchShopsWithCashbackUseCase> {
        FetchShopsWithCashbackUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<SearchShopsUseCase> {
        SearchShopsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}