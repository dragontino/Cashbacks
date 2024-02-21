package com.cashbacks.domain.usecase.cashbacks

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchCashbacks(query: String): List<Pair<Pair<String, String>, Cashback>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> listOf()
                else -> repository.searchCashbacks(query)
            }
        }
    }
}