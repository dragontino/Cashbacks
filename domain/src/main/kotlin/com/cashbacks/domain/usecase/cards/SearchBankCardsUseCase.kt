package com.cashbacks.domain.usecase.cards

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchBankCardsUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchBankCards(query: String): List<BankCard> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> listOf()
                else -> repository.searchBankCards(query)
            }
        }
    }
}