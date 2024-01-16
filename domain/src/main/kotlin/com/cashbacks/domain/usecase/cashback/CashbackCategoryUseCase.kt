package com.cashbacks.domain.usecase.cashback

import android.util.Log
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CashbackCategoryUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "CashbackCategoryUseCase"
    }

    suspend fun deleteCashbackFromCategory(
        categoryId: Long,
        cashback: Cashback,
        errorMessage: (String) -> Unit
    ) {
        withContext(dispatcher) {
            repository
                .deleteCashbackFromCategory(categoryId, cashback)
                .exceptionOrNull()
                .also { Log.e(TAG, it?.message, it) }
                ?.message
                ?.let(errorMessage)
        }
    }
}