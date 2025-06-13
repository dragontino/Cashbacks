package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface GetCashbackUseCase {
    suspend operator fun invoke(cashbackId: Long): Result<FullCashback>
}


internal class GetCashbackUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : GetCashbackUseCase {
    private companion object {
        const val TAG = "GetCashbackUseCase"
    }

    override suspend fun invoke(cashbackId: Long): Result<FullCashback> {
        return withContext(dispatcher) {
            repository.getCashbackById(cashbackId).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}