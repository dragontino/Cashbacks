package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AddShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addShop(shop: CategoryShop): Result<Long> {
        return withContext(dispatcher) {
            repository.addShop(shop)
        }
    }

    suspend fun addShop(categoryId: Long, shop: Shop) : Result<Long> {
        return withContext(dispatcher) {
            repository.addShop(categoryId, shop)
        }
    }
}