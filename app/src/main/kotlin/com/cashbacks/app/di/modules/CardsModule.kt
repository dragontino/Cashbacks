package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.BankCardRepository
import com.cashbacks.domain.usecase.cards.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.cards.EditBankCardUseCase
import com.cashbacks.domain.usecase.cards.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.cards.GetBankCardUseCase
import com.cashbacks.domain.usecase.cards.SearchBankCardsUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class CardsModule {
    @Provides
    fun provideFetchBankCardsUseCase(bankCardRepository: BankCardRepository) =
        FetchBankCardsUseCase(
            repository = bankCardRepository,
            dispatcher = Dispatchers.IO
        )

    @Provides
    fun provideGetBankCardUseCase(bankCardRepository: BankCardRepository) = GetBankCardUseCase(
        repository = bankCardRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideEditBankCardUseCase(bankCardRepository: BankCardRepository) = EditBankCardUseCase(
        repository = bankCardRepository,
        dispatcher = Dispatchers.IO
    )

    @Provides
    fun provideSearchBankCardsUseCase(bankCardRepository: BankCardRepository) =
        SearchBankCardsUseCase(repository = bankCardRepository, dispatcher = Dispatchers.IO)

    @Provides
    fun provideDeleteBankCardUseCase(bankCardRepository: BankCardRepository) = DeleteBankCardUseCase(
        repository = bankCardRepository,
        dispatcher = Dispatchers.IO
    )
}