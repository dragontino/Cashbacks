package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CashbackShopUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addCashbacksToShop(shopId: Long, cashbacks: List<Cashback>): List<Result<Unit>> {
        return withContext(dispatcher) {
            when {
                cashbacks.isEmpty() -> listOf()
                else -> repository.addCashbacksToShop(shopId, cashbacks)
            }
        }
    }

    suspend fun updateCashbacksInShop(shopId: Long, cashbacks: List<Cashback>): Result<Unit> {
        return withContext(dispatcher) {
            when {
                cashbacks.isEmpty() -> Result.success(Unit)
                else -> repository.updateCashbacksInCategory(shopId, cashbacks)
            }
        }
    }

    suspend fun deleteCashbacksFromShop(shopId: Long, cashbacks: List<Cashback>): Result<Unit> {
        return withContext(dispatcher) {
            when {
                cashbacks.isEmpty() -> Result.success(Unit)
                else -> repository.deleteCashbacksFromCategory(shopId, cashbacks)
            }
        }
    }
}