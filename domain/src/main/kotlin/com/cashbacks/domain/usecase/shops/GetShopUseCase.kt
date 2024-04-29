package com.cashbacks.domain.usecase.shops

import android.util.Log
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "ShopUseCase"
    }

    suspend fun getShopById(id: Long): Result<Shop> = withContext(dispatcher) {
        val result = repository.getShopById(id)
        result.exceptionOrNull()?.let { Log.e(TAG, it.localizedMessage, it) }
        return@withContext result
    }

    suspend fun getShopWithCategoryById(id: Long): Result<CategoryShop> = withContext(dispatcher) {
        val result = repository.getShopWithCategoryById(id)
        result.exceptionOrNull()?.let { Log.e(TAG, it.localizedMessage, it) }
        return@withContext result
    }
}