package com.cashbacks.features.category.domain.usecase

import android.util.Log
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchCategoriesWithCashbackUseCase {
    operator fun invoke(): Flow<List<Category>>
}


internal class FetchCategoriesWithCashbackUseCaseImpl(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchCategoriesWithCashbackUseCase {
    private companion object {
        const val TAG = "FetchCategoriesWithCashbackUseCase"
    }

    override fun invoke(): Flow<List<Category>> {
        return repository
            .fetchCategoriesWithCashback()
            .flowOn(dispatcher)
            .catch { throwable ->
                Log.e(TAG, throwable.message, throwable)
                throw throwable
            }
    }
}