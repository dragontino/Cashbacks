package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SearchCashbacksUseCase {
    suspend operator fun invoke(query: String): Result<List<FullCashback>>
}


internal class SearchCashbacksUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : SearchCashbacksUseCase {
    private companion object {
        const val TAG = "SearchCashbacksUseCase"
    }

    override suspend fun invoke(query: String): Result<List<FullCashback>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> Result.success(emptyList())
                else -> repository.searchCashbacks(query).onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }
}