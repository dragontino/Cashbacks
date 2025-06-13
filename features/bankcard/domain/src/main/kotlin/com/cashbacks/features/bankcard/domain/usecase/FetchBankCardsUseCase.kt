package com.cashbacks.features.bankcard.domain.usecase

import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

sealed interface FetchBankCardsUseCase {
    operator fun invoke(): Flow<List<PrimaryBankCard>>
}

internal class FetchBankCardsUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchBankCardsUseCase {

    override fun invoke(): Flow<List<PrimaryBankCard>> {
        return repository.fetchAllBankCards().flowOn(dispatcher)
    }
}