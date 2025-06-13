package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateCategoryUseCase {
    suspend operator fun invoke(category: Category): Result<Unit>
}


internal class UpdateCategoryUseCaseImpl(
    private val categoryRepository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateCategoryUseCase {
    private companion object {
        const val TAG = "UpdateCategoriesUseCase"
    }

    override suspend fun invoke(category: Category): Result<Unit> = withContext(dispatcher) {
        categoryRepository.updateCategory(category).onFailure {
            Log.e(TAG, it.message, it)
        }
    }
}