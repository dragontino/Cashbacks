package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun updateShop(
        shop: Shop,
        errorMessage: (String) -> Unit
    ) {
        return withContext(dispatcher) {
            repository
                .updateShop(shop)
                .exceptionOrNull()
                ?.message
                ?.let(errorMessage)
        }
    }

    suspend fun getShopById(id: Long): Result<Shop> {
        return withContext(dispatcher) {
            repository.getShopById(id)
        }
    }
}