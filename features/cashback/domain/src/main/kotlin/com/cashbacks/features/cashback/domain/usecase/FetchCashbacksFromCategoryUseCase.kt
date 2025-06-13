package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchCashbacksFromCategoryUseCase {
    operator fun invoke(categoryId: Long): Flow<List<BasicCashback>>
}


internal class FetchCashbacksFromCategoryUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchCashbacksFromCategoryUseCase {
    private companion object {
        const val TAG = "FetchCashbacksFromCategoryUseCase"
    }

    override fun invoke(categoryId: Long): Flow<List<BasicCashback>> {
        return repository.fetchCashbacksFromCategory(categoryId)
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}