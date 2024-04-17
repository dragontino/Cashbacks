package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchAllShopsUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchAllShops(): Flow<List<CategoryShop>> {
        return repository.fetchAllShops().flowOn(dispatcher)
    }

    fun fetchShopsWithCashback(): Flow<List<CategoryShop>> {
        return repository.fetchShopsWithCashbacks().flowOn(dispatcher)
    }
}