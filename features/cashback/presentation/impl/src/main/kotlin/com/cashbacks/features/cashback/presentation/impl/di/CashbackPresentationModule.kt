package com.cashbacks.features.cashback.presentation.impl.di

import com.cashbacks.features.cashback.presentation.impl.CashbackViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CashbackPresentationModule = module {
    viewModel<CashbackViewModel> { params ->
        CashbackViewModel(
            getCashback = get(),
            addCashbackToCategory = get(),
            addCashbackToShop = get(),
            updateCashbackInCategory = get(),
            updateCashbackInShop = get(),
            deleteCashback = get(),
            fetchAllCategories = get(),
            getCategory = get(),
            getShop = get(),
            fetchAllShops = get(),
            fetchBankCards = get(),
            getMeasureUnits = get(),
            addCategory = get(),
            messageHandler = get(),
            stateHandle = get(),
            storeFactory = get(),
            ownerType = params.get(),
            initialOwnerId = params.getOrNull(),
            initialCashbackId = params.getOrNull(),
        )
    }
}