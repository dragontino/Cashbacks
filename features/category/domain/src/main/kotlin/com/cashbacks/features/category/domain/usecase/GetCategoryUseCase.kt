package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetCategoryUseCase {
    suspend operator fun invoke(id: Long): Result<Category>
}


internal class GetCategoryUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : GetCategoryUseCase {
    private companion object {
        const val TAG = "GetCategoryUseCase"
    }

    override suspend fun invoke(id: Long): Result<Category> = withContext(dispatcher) {
        repository.getCategoryById(id).onFailure {
            Log.e(TAG, it.message, it)
        }
    }
}