package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface AddCashbackToCategoryUseCase {
    suspend operator fun invoke(categoryId: Long, cashback: Cashback): Result<Long>
}


internal class AddCashbackToCategoryUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : AddCashbackToCategoryUseCase {
    private companion object {
        const val TAG = "AddCashbackToCategoryUseCase"
    }

    override suspend fun invoke(categoryId: Long, cashback: Cashback): Result<Long> {
        return withContext(dispatcher) {
            repository.addCashbackToCategory(categoryId, cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}