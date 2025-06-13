package com.cashbacks.features.category.domain.di

import com.cashbacks.features.category.domain.usecase.AddCategoryUseCase
import com.cashbacks.features.category.domain.usecase.AddCategoryUseCaseImpl
import com.cashbacks.features.category.domain.usecase.DeleteCategoryUseCase
import com.cashbacks.features.category.domain.usecase.DeleteCategoryUseCaseImpl
import com.cashbacks.features.category.domain.usecase.FetchAllCategoriesUseCase
import com.cashbacks.features.category.domain.usecase.FetchAllCategoriesUseCaseImpl
import com.cashbacks.features.category.domain.usecase.FetchCategoriesWithCashbackUseCase
import com.cashbacks.features.category.domain.usecase.FetchCategoriesWithCashbackUseCaseImpl
import com.cashbacks.features.category.domain.usecase.FetchCategoryUseCase
import com.cashbacks.features.category.domain.usecase.FetchCategoryUseCaseImpl
import com.cashbacks.features.category.domain.usecase.GetCategoryUseCase
import com.cashbacks.features.category.domain.usecase.GetCategoryUseCaseImpl
import com.cashbacks.features.category.domain.usecase.SearchCategoriesUseCase
import com.cashbacks.features.category.domain.usecase.SearchCategoriesUseCaseImpl
import com.cashbacks.features.category.domain.usecase.UpdateCategoryUseCase
import com.cashbacks.features.category.domain.usecase.UpdateCategoryUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val CategoryDomainModule = module {
    single<AddCategoryUseCase> {
        AddCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchAllCategoriesUseCase> {
        FetchAllCategoriesUseCaseImpl(
            repository = get(), dispatcher = Dispatchers.IO
        )
    }

    single<FetchCategoriesWithCashbackUseCase> {
        FetchCategoriesWithCashbackUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<SearchCategoriesUseCase> {
        SearchCategoriesUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<UpdateCategoryUseCase> {
        UpdateCategoryUseCaseImpl(
            categoryRepository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetCategoryUseCase> {
        GetCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchCategoryUseCase> {
        FetchCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<DeleteCategoryUseCase> {
        DeleteCategoryUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}