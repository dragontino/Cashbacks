package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchAllCashbacksUseCase {
    operator fun invoke(): Flow<List<FullCashback>>
}


internal class FetchAllCashbacksUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchAllCashbacksUseCase {
    private companion object {
        const val TAG = "FetchAllCashbacksUseCase"
    }

    override fun invoke(): Flow<List<FullCashback>> {
        return repository.fetchAllCashbacks()
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}