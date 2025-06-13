package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.filterMaxCashbacks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetMaxCashbacksFromCategoryUseCase {
    suspend operator fun invoke(categoryId: Long): Result<Set<Cashback>>
}


internal class GetMaxCashbacksFromCategoryUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : GetMaxCashbacksFromCategoryUseCase {
    private companion object {
        const val TAG = "GetMaxCashbacksFromCategoryUseCase"
    }

    override suspend fun invoke(categoryId: Long): Result<Set<Cashback>> = withContext(dispatcher) {
        return@withContext repository
            .getAllCashbacksFromCategory(categoryId)
            .map { it.filterMaxCashbacks() }
            .onFailure {
                Log.e(TAG, it.message, it)
            }
    }
}