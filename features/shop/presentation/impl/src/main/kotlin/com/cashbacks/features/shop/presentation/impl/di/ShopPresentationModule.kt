package com.cashbacks.features.shop.presentation.impl.di

import com.cashbacks.features.shop.presentation.impl.viewmodel.ShopViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ShopPresentationModule = module {
    viewModel<ShopViewModel> { params ->
        ShopViewModel(
            fetchCashbacksFromShop = get(),
            fetchAllCategories = get(),
            addCategory = get(),
            getShop = get(),
            addShop = get(),
            updateShop = get(),
            deleteShop = get(),
            deleteCashback = get(),
            messageHandler = get(),
            storeFactory = get(),
            initialShopId = params.getOrNull(),
            initialIsEditing = params.get(),
        )
    }
}