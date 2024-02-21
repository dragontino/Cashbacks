package com.cashbacks.domain.usecase.cashbacks

import android.util.Log
import com.cashbacks.domain.model.ExpiredCashbacksDeletionException
import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.util.parseToDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DeleteExpiredCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteExpiredCashbacksUseCase"
    }

    suspend fun deleteExpiredCashbacks(
        nowDate: LocalDate,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        withContext(dispatcher) {
            val expiredCashbacks = repository.getAllCashbacks().filter {
                when (val expirationDate = it.expirationDate?.parseToDate()) {
                    null -> false
                    else -> expirationDate < nowDate
                }
            }
            if (expiredCashbacks.isNotEmpty()) {
                val result = repository.deleteCashbacks(expiredCashbacks)
                result.exceptionOrNull()?.let {
                    Log.e(TAG, it.message, it)
                    onFailure(ExpiredCashbacksDeletionException)
                }
                result.getOrNull()?.let { onSuccess() }
            }
        }
    }
}