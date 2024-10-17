package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchShopsFromCategoryUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<BasicShop>> {
        return repository.fetchAllShopsFromCategory(categoryId).flowOn(dispatcher)
    }
}