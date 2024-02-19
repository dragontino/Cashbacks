package com.cashbacks.domain.usecase.shops

import android.util.Log
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteShopUseCase"
    }

    suspend fun deleteShop(shop: Shop): Result<Unit> = withContext(dispatcher) {
        repository.deleteShop(shop).also { result ->
            result.exceptionOrNull()?.let {
                Log.e(TAG, it.message, it)
            }
        }
    }
}