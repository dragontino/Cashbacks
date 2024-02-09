package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchShopsFromCategoryUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>> {
        return repository.fetchAllShopsFromCategory(categoryId).flowOn(dispatcher)
    }

    fun fetchShopsWithCashbacksFromCategory(categoryId: Long): Flow<List<Shop>> {
        return repository.fetchShopsWithCashbackFromCategory(categoryId).flowOn(dispatcher)
    }
}