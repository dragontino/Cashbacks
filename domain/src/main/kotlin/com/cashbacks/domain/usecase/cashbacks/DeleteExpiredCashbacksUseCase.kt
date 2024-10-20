package com.cashbacks.domain.usecase.cashbacks

import android.util.Log
import com.cashbacks.domain.model.ExpiredCashbacksDeletionException
import com.cashbacks.domain.repository.CashbackRepository
import com.cashbacks.domain.util.parseToDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DeleteExpiredCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteExpiredCashbacksUseCase"
    }

    // TODO: переделать через планирование задач и с TimeZone
    suspend fun deleteExpiredCashbacks(
        today: LocalDate,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        withContext(dispatcher) {
            val expiredCashbacks = repository.getAllCashbacks().filter {
                when (val expirationDate = it.expirationDate?.parseToDate()) {
                    null -> false
                    else -> expirationDate < today
                }
            }

            if (expiredCashbacks.isNotEmpty()) {
                repository.deleteCashbacks(expiredCashbacks)
                    .onSuccess { onSuccess() }
                    .onFailure {
                        Log.e(TAG, it.message, it)
                        onFailure(ExpiredCashbacksDeletionException)
                    }
            }
        }
    }
}