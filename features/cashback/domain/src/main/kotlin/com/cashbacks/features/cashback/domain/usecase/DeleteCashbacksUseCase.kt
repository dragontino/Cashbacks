package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface DeleteCashbacksUseCase {
    suspend operator fun invoke(cashbacks: List<Cashback>): Result<Int>
}


internal class DeleteCashbacksUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteCashbacksUseCase {
    private companion object {
        const val TAG = "DeleteCashbacksUseCase"
    }

    override suspend fun invoke(cashbacks: List<Cashback>): Result<Int> {
        return withContext(dispatcher) {
            repository.deleteCashbacks(cashbacks).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}