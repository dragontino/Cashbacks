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

    suspend fun updateCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateCashbackInCategory(categoryId, cashback)
        }
    }

    suspend fun updateCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateCashbackInShop(shopId, cashback)
        }
    }

    suspend fun getCashbackById(id: Long): Result<Cashback> {
        return withContext(dispatcher) {
            repository.getCashbackById(id)
        }
    }
}