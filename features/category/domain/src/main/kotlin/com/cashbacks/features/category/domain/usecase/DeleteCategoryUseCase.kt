package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface DeleteCategoryUseCase {
    suspend operator fun invoke(category: Category): Result<Unit>
}


internal class DeleteCategoryUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteCategoryUseCase {
    private companion object {
        const val TAG = "DeleteCategoryUseCase"
    }

    override suspend fun invoke(category: Category): Result<Unit> = withContext(dispatcher) {
        repository.deleteCategory(category).onFailure {
            Log.e(TAG, it.message, it)
        }
    }
}