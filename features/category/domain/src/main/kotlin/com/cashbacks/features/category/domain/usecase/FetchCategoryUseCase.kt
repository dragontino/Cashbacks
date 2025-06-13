package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchCategoryUseCase {
    operator fun invoke(id: Long): Flow<Category>
}

internal class FetchCategoryUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchCategoryUseCase {
    private companion object {
        const val TAG = "FetchCategoryUseCase"
    }

    override fun invoke(id: Long): Flow<Category> {
        return repository.fetchCategoryById(id)
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}