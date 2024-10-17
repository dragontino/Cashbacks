package com.cashbacks.domain.usecase.categories

import android.util.Log
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCategory
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "GetCategoryUseCase"
    }

    fun fetchCategoryById(
        id: Long,
        onFailure: (Throwable) -> Unit = {}
    ): Flow<FullCategory> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }
        return repository
            .fetchCategoryById(id)
            .flowOn(handler + dispatcher)
    }


    suspend fun getCategoryById(id: Long): Result<Category> {
        return repository.getCategoryById(id).onFailure {
            Log.e(TAG, it.message, it)
        }
    }
}