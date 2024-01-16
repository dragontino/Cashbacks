package com.cashbacks.domain.usecase.categories

import android.util.Log
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteCategoryUseCase"
    }

    suspend fun deleteCategory(category: Category, errorMessage: (String) -> Unit = {}) {
        withContext(dispatcher) {
            repository
                .deleteCategory(category)
                .exceptionOrNull()
                ?.let { exception ->
                    Log.e(TAG, exception.message, exception)
                    exception.message?.let(errorMessage)
                }
        }
    }
}