package com.cashbacks.domain.usecase.categories

import android.util.Log
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "UpdateCategoriesUseCase"
    }

    suspend fun updateCategory(
        category: Category,
        exceptionMessage: (Throwable) -> Unit
    ): Result<Unit> = withContext(dispatcher) {
        val result = categoryRepository.updateCategory(category)

        result.exceptionOrNull()
            ?.also { Log.e(TAG, it.message, it) }
            ?.let(exceptionMessage)

        return@withContext result
    }
}