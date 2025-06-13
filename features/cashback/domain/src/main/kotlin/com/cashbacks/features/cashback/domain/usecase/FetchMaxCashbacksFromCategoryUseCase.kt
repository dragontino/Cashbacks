package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.filterMaxCashbacks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface FetchMaxCashbacksFromCategoryUseCase {
    operator fun invoke(categoryId: Long): Flow<Set<Cashback>>
}


internal class FetchMaxCashbacksFromCategoryUseCaseImpl(
    private val fetchAllCashbacksFromCategory: FetchCashbacksFromCategoryUseCase,
) : FetchMaxCashbacksFromCategoryUseCase {
    private companion object {
        const val TAG = "FetchMaxCashbacksFromCategoryUseCase"
    }

    override fun invoke(categoryId: Long): Flow<Set<Cashback>> {
        return fetchAllCashbacksFromCategory(categoryId)
            .map { it.filterMaxCashbacks() }
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}