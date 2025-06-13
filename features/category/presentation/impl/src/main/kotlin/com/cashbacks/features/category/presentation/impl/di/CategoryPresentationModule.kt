package com.cashbacks.features.category.presentation.impl.di

import com.cashbacks.features.category.presentation.impl.viewmodel.CategoryEditingViewModel
import com.cashbacks.features.category.presentation.impl.viewmodel.CategoryViewingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CategoryPresentationModule = module {
    viewModel<CategoryViewingViewModel> { params ->
        CategoryViewingViewModel(
            fetchCategory = get(),
            fetchShopsFromCategory = get(),
            fetchCashbacksFromCategory = get(),
            getMaxCashbacksFromShop = get(),
            deleteShop = get(),
            deleteCashback = get(),
            storeFactory = get(),
            categoryId = params.get(),
            startTab = params.get()
        )
    }

    viewModel<CategoryEditingViewModel> { params ->
        CategoryEditingViewModel(
            getCategory = get(),
            addShop = get(),
            updateCategory = get(),
            deleteCategory = get(),
            fetchShopsFromCategory = get(),
            fetchCashbacksFromCategory = get(),
            getMaxCashbacksFromShop = get(),
            deleteShop = get(),
            deleteCashback = get(),
            storeFactory = get(),
            categoryId = params.get(),
            stateHandle = get(),
            messageHandler = get(),
            startTab = params.get()
        )
    }
}