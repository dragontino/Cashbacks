package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

interface DeleteExpiredCashbacksUseCase {
    // TODO: переделать через планирование задач и с TimeZone
    suspend operator fun invoke(today: LocalDate): Result<Int>
}


internal class DeleteExpiredCashbacksUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteExpiredCashbacksUseCase {
    private companion object {
        const val TAG = "DeleteExpiredCashbacksUseCase"
    }

    override suspend fun invoke(today: LocalDate): Result<Int> {
        return withContext(dispatcher) {
            val allCashbacksResult = repository.getAllCashbacks()
            allCashbacksResult.exceptionOrNull()?.let {
                Log.e(TAG, it.message, it)
                return@withContext Result.failure(it)
            }

            val expiredCashbacks = allCashbacksResult.getOrNull()!!.filter {
                when (val expirationDate = it.expirationDate) {
                    null -> false
                    else -> expirationDate < today
                }
            }
            if (expiredCashbacks.isEmpty()) {
                return@withContext Result.success(0)
            }

            return@withContext repository.deleteCashbacks(expiredCashbacks)
                .onFailure { Log.e(TAG, it.message, it) }
        }
    }
}