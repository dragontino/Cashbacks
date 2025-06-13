package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SearchCategoriesUseCase {
    suspend operator fun invoke(query: String, cashbacksRequired: Boolean): Result<List<Category>>
}


internal class SearchCategoriesUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : SearchCategoriesUseCase {
    private companion object {
        const val TAG = "SearchCategoriesUseCase"
    }

    override suspend fun invoke(query: String, cashbacksRequired: Boolean): Result<List<Category>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> return@withContext Result.success(emptyList())
                else -> repository.searchCategories(query, cashbacksRequired).onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }
}