package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.filterMaxCashbacks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetMaxCashbacksFromShopUseCase {
    suspend operator fun invoke(shopId: Long): Result<Set<Cashback>>
}


internal class GetMaxCashbacksFromShopUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : GetMaxCashbacksFromShopUseCase {
    private companion object {
        const val TAG = "GetMaxCashbacksFromShopUseCase"
    }

    override suspend fun invoke(shopId: Long): Result<Set<Cashback>> = withContext(dispatcher) {
        return@withContext repository.getAllCashbacksFromShop(shopId)
            .map { it.filterMaxCashbacks() }
            .onFailure {
                Log.e(TAG, it.message, it)
            }
    }
}