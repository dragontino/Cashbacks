package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateCashbackInCategoryUseCase {
    suspend operator fun invoke(categoryId: Long, cashback: Cashback): Result<Unit>
}


internal class UpdateCashbackInCategoryUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateCashbackInCategoryUseCase {
    private companion object {
        const val TAG = "UpdateCashbackInCategoryUseCase"
    }

    override suspend fun invoke(categoryId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateCashbackInCategory(categoryId, cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}