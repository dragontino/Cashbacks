package com.cashbacks.domain.usecase.cashback

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CashbackShopUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun deleteCashbackFromShop(shopId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteCashbackFromShop(shopId, cashback)
        }
    }
}