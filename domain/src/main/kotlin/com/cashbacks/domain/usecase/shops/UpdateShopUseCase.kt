package com.cashbacks.domain.usecase.shops

import android.util.Log
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class UpdateShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "ShopUseCase"
    }

    suspend fun updateShop(categoryId: Long, shop: Shop): Result<Unit> {
        return withContext(dispatcher) {
            val result = repository.updateShop(categoryId, shop)
            result.exceptionOrNull()?.let { Log.e(TAG, it.localizedMessage, it) }
            return@withContext result
        }
    }
}