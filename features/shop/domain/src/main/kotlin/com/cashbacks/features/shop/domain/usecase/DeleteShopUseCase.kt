package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface DeleteShopUseCase {
    suspend operator fun invoke(shop: Shop): Result<Unit>
}


internal class DeleteShopUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteShopUseCase {
    private companion object {
        const val TAG = "DeleteShopUseCase"
    }

    override suspend fun invoke(shop: Shop): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteShop(shop).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}