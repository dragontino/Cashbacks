package com.cashbacks.features.bankcard.presentation.impl.di

import com.cashbacks.features.bankcard.presentation.impl.viewmodel.BankCardEditingViewModel
import com.cashbacks.features.bankcard.presentation.impl.viewmodel.BankCardViewingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val BankCardPresentationModule = module {
    viewModel<BankCardViewingViewModel> { params ->
        BankCardViewingViewModel(
            fetchBankCardUseCase = get(),
            deleteBankCardUseCase = get(),
            storeFactory = get(),
            cardId = params.get()
        )
    }

    viewModel<BankCardEditingViewModel> { params ->
        BankCardEditingViewModel(
            getBankCardUseCase = get(),
            addBankCardUseCase = get(),
            updateBankCardUseCase = get(),
            messageHandler = get(),
            storeFactory = get(),
            bankCardId = params.getOrNull()
        )
    }
}