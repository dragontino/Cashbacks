package com.cashbacks.domain.usecase.cashbacks

import android.util.Log
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteCashbackUseCase"
    }

    suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteCashback(cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}