package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CashbackCategoryUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addCashbacksToCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.addCashbacksToCategory(categoryId, cashbacks)
        }
    }

    suspend fun updateCashbacksInCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.updateCashbacksInCategory(categoryId, cashbacks)
        }
    }

    suspend fun deleteCashbacksFromCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.deleteCashbacksFromCategory(categoryId, cashbacks)
        }
    }
}