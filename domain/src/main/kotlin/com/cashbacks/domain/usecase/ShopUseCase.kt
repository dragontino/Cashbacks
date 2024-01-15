package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ShopUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addShopsInCategory(
        categoryId: Long,
        shops: List<BasicShop>,
    ): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.addShopsToCategory(categoryId, shops)
        }
    }


    suspend fun updateShopsInCategory(
        categoryId: Long,
        shops: List<BasicShop>
    ): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.updateShopsInCategory(categoryId, shops)
        }
    }


    suspend fun deleteShopsFromCategory(
        categoryId: Long,
        shops: List<BasicShop>
    ): List<Result<Unit>> {
        return withContext(dispatcher) {
            repository.deleteShopsFromCategory(categoryId, shops)
        }
    }
}