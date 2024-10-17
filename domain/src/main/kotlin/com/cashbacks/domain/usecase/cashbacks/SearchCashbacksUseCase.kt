package com.cashbacks.domain.usecase.cashbacks

import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchCashbacks(query: String): List<FullCashback> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> listOf()
                else -> repository.searchCashbacks(query)
            }
        }
    }
}