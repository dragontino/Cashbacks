package com.cashbacks.features.bankcard.domain.di

import com.cashbacks.features.bankcard.domain.usecase.AddBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.AddBankCardUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.DeleteBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.DeleteBankCardUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardByIdUseCase
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardByIdUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardsUseCase
import com.cashbacks.features.bankcard.domain.usecase.FetchBankCardsUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.GetBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.GetBankCardUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.SearchBankCardsUseCase
import com.cashbacks.features.bankcard.domain.usecase.SearchBankCardsUseCaseImpl
import com.cashbacks.features.bankcard.domain.usecase.UpdateBankCardUseCase
import com.cashbacks.features.bankcard.domain.usecase.UpdateBankCardUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val BankCardDomainModule = module {
    single<AddBankCardUseCase> {
        AddBankCardUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<UpdateBankCardUseCase> {
        UpdateBankCardUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchBankCardsUseCase> {
        FetchBankCardsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetBankCardUseCase> {
        GetBankCardUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchBankCardByIdUseCase> {
        FetchBankCardByIdUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<DeleteBankCardUseCase> {
        DeleteBankCardUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<SearchBankCardsUseCase> {
        SearchBankCardsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

}