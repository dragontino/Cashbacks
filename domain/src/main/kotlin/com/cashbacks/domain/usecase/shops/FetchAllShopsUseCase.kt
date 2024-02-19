package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchAllShopsUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchAllShops(): Flow<List<Pair<Category, Shop>>> {
        return repository.fetchAllShopsWithCategories().flowOn(dispatcher)
    }

    fun fetchShopsWithCashback(): Flow<List<Pair<Category, Shop>>> {
        return repository.fetchShopsWithCategoriesAndCashbacks().flowOn(dispatcher)
    }
}