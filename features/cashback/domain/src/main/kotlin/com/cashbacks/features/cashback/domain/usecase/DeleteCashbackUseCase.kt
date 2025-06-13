package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface DeleteCashbackUseCase {
    suspend operator fun invoke(cashback: Cashback): Result<Unit>
}


internal class DeleteCashbackUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteCashbackUseCase {
    private companion object {
        const val TAG = "DeleteCashbackUseCase"
    }

    override suspend fun invoke(cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteCashback(cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}