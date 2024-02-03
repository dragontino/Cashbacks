package com.cashbacks.domain.usecase.card

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.flow.Flow

class FetchBankCardsUseCase(private val repository: BankCardRepository) {
    fun fetchBankCards(): Flow<List<BankCard>> {
        return repository.fetchBankCards()
    }
}