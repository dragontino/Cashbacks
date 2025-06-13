package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface AddCashbackToShopUseCase {
    suspend operator fun invoke(shopId: Long, cashback: Cashback): Result<Long>
}


internal class AddCashbackToShopUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : AddCashbackToShopUseCase {
    private companion object {
        const val TAG = "AddCashbackToShopUseCase"
    }

    override suspend fun invoke(shopId: Long, cashback: Cashback): Result<Long> {
        return withContext(dispatcher) {
            repository.addCashbackToShop(shopId, cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}