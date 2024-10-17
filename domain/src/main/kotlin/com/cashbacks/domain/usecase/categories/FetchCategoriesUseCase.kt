package com.cashbacks.domain.usecase.categories

import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchCategoriesUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchAllCategories(): Flow<List<BasicCategory>> {
        return repository.fetchAllCategories().flowOn(dispatcher)
    }

    fun fetchCategoriesWithCashback(): Flow<List<BasicCategory>> {
        return repository.fetchCategoriesWithCashback().flowOn(dispatcher)
    }
}