package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun getShopById(id: Long): Result<Shop> {
        return withContext(dispatcher) {
            repository.getShopById(id)
        }
    }
}