package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchShopsUseCase(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchShops(query: String, cashbacksRequired: Boolean): List<Pair<Category, Shop>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> listOf()
                else -> repository.searchShops(query, cashbacksRequired)
            }
        }
    }
}