package com.cashbacks.domain.usecase.cashback

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditCashbackUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.addCashbackToCategory(categoryId, cashback)
        }
    }

    suspend fun addCashbackToShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.addCashbackToShop(shopId, cashback)
        }
    }

    suspend fun updateCashback(cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateCashback(cashback)
        }
    }

    suspend fun getCashbackById(id: Long): Result<Cashback> {
        return withContext(dispatcher) {
            repository.getCashbackById(id)
        }
    }
}