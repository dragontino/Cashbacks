package com.cashbacks.domain.usecase.cashback

import android.util.Log
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CashbackShopUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "CashbackShopUseCase"
    }

    suspend fun deleteCashbackFromShop(
        shopId: Long,
        cashback: Cashback,
        errorMessage: (String) -> Unit
    ) {
        withContext(dispatcher) {
            repository
                .deleteCashbackFromShop(shopId, cashback)
                .exceptionOrNull()
                .also { Log.e(TAG, it?.message, it) }
                ?.message
                ?.let(errorMessage)
        }
    }
}