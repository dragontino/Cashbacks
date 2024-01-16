package com.cashbacks.domain.usecase.categories

import android.util.Log
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "CategoriesUseCase"
    }

    suspend fun updateCategory(category: Category): Result<Unit> {
        return withContext(dispatcher) {
            categoryRepository.updateCategory(category)
        }
    }

    suspend fun getCategoryById(id: Long): Result<Category> = withContext(dispatcher) {
        return@withContext categoryRepository.getCategoryById(id)
            .also { result ->
                result.exceptionOrNull()?.let { Log.e(TAG, it.message, it) }
            }
    }
}