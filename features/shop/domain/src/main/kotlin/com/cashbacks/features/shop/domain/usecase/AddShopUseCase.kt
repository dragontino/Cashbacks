package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface AddShopUseCase {
    suspend operator fun invoke(shop: CategoryShop): Result<Long>
    suspend operator fun invoke(categoryId: Long, shop: Shop): Result<Long>
}


internal class AddShopUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : AddShopUseCase {
    private companion object {
        const val TAG = "AddShopUseCase"
    }

    override suspend fun invoke(shop: CategoryShop): Result<Long> {
        return withContext(dispatcher) {
            repository.addShop(shop).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }

    override suspend fun invoke(categoryId: Long, shop: Shop) : Result<Long> {
        return withContext(dispatcher) {
            repository.addShop(categoryId, shop).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}