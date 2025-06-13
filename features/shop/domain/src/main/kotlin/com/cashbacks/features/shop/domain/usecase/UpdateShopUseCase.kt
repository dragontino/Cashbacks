package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateShopUseCase {
    suspend operator fun invoke(shop: CategoryShop): Result<Unit>
}


internal class UpdateShopUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateShopUseCase {
    private companion object {
        const val TAG = "UpdateShopUseCase"
    }

    override suspend fun invoke(shop: CategoryShop): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateShop(shop).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}