package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun updateShopInCategory(
        categoryId: Long,
        shop: Shop
    ): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateShopInCategory(categoryId, shop)
        }
    }

    suspend fun getShopById(id: Long): Result<Shop> {
        return withContext(dispatcher) {
            repository.getShopById(id)
        }
    }
}