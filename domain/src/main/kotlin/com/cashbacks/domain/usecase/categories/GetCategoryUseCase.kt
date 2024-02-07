package com.cashbacks.domain.usecase.categories

import android.util.Log
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "GetCategoryUseCase"
    }

    suspend fun getCategoryById(id: Long): Result<Category> = withContext(dispatcher) {
        return@withContext repository.getCategoryById(id)
            .also { result ->
                result.exceptionOrNull()?.let { Log.e(TAG, it.message, it) }
            }
    }
}