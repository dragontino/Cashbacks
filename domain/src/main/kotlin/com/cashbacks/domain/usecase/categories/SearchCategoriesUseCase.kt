package com.cashbacks.domain.usecase.categories

import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchCategoriesUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchCategories(query: String, cashbacksRequired: Boolean): List<BasicCategory> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> return@withContext emptyList()
                else -> repository.searchCategories(query, cashbacksRequired)
            }
        }
    }
}