package com.cashbacks.domain.usecase.cards

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchBankCardsUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchBankCards(): Flow<List<BankCard>> {
        return repository.fetchBankCards().flowOn(dispatcher)
    }
}