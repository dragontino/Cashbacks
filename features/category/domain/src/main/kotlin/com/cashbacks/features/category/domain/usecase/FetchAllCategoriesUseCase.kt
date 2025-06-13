package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchAllCategoriesUseCase {
    operator fun invoke(): Flow<List<Category>>
}


internal class FetchAllCategoriesUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchAllCategoriesUseCase {
    private companion object {
        const val TAG = "FetchAllCategoriesUseCase"
    }

    override fun invoke(): Flow<List<Category>> {
        return repository.fetchAllCategories()
            .flowOn(dispatcher)
            .catch { throwable ->
                Log.e(TAG, throwable.message, throwable)
                throw throwable
            }
    }
}