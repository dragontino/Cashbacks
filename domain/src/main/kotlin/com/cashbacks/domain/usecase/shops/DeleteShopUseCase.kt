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

    suspend fun deleteShopFromCategory(
        categoryId: Long,
        shop: Shop,
        errorMessage: (String) -> Unit = {}
    ) {
        withContext(dispatcher) {
            repository
                .deleteShopFromCategory(categoryId, shop)
                .exceptionOrNull()
                .also { Log.e(TAG, it?.message, it) }
                ?.message
                ?.let(errorMessage)
        }
    }
}