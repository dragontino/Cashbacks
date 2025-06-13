package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface AddCategoryUseCase {
    suspend operator fun invoke(category: Category): Result<Long>
}


internal class AddCategoryUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : AddCategoryUseCase {

    private companion object {
        const val TAG = "AddCategoryUseCase"
    }

    override suspend fun invoke(category: Category): Result<Long> {
        return withContext(dispatcher) {
            repository.addCategory(category).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}