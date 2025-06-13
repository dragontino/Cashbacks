package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetShopUseCase {
    suspend operator fun invoke(shopId: Long): Result<CategoryShop>
}


internal class GetShopUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : GetShopUseCase {
    private companion object {
        const val TAG = "ShopUseCase"
    }

    override suspend fun invoke(shopId: Long): Result<CategoryShop> {
        return withContext(dispatcher) {
            repository.getShopById(shopId).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}