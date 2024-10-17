package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchCategoryShopsUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchAllShops(): Flow<List<BasicCategoryShop>> {
        return repository.fetchAllShops().flowOn(dispatcher)
    }

    fun fetchShopsWithCashback(): Flow<List<BasicCategoryShop>> {
        return repository.fetchShopsWithCashback().flowOn(dispatcher)
    }
}