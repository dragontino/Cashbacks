package com.cashbacks.domain.usecase.shops

import android.util.Log
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "EditShopUseCase"
    }

    suspend fun updateShop(shop: Shop): Result<Unit> {
        return withContext(dispatcher) {
            val result = repository.updateShop(shop)
            result.exceptionOrNull()?.let { Log.e(TAG, it.localizedMessage, it) }
            return@withContext result
        }
    }

    suspend fun getShopById(id: Long): Result<CategoryShop> {
        return withContext(dispatcher) {
            repository.getShopById(id)
        }
    }
}