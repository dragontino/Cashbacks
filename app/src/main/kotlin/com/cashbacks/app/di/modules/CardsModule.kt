package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.BankCardRepository
import com.cashbacks.domain.usecase.card.DeleteBankCardUseCase
import com.cashbacks.domain.usecase.card.EditBankCardUseCase
import com.cashbacks.domain.usecase.card.FetchBankCardsUseCase
import com.cashbacks.domain.usecase.card.GetBankCardUseCase
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
    fun provideDeleteBankCardUseCase(bankCardRepository: BankCardRepository) = DeleteBankCardUseCase(
        repository = bankCardRepository,
        dispatcher = Dispatchers.IO
    )
}