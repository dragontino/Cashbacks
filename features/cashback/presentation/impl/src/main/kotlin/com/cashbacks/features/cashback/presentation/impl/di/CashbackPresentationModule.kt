package com.cashbacks.features.cashback.presentation.impl.di

import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.impl.CashbackViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CashbackPresentationModule = module {
    viewModel<CashbackViewModel> { params ->
        val args = params.get<CashbackArgs>()
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
            ownerType = args.ownerType,
            initialOwnerId = args.ownerId,
            initialCashbackId = args.cashbackId,
        )
    }
}