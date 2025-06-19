package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

interface GetExpiredCashbacksUseCase {
    suspend operator fun invoke(today: LocalDate): Result<List<Cashback>>
}


internal class GetExpiredCashbacksUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : GetExpiredCashbacksUseCase {
    private companion object {
        const val TAG = "GetExpiredCashbacksUseCase"
    }

    override suspend fun invoke(today: LocalDate): Result<List<Cashback>> {
        return withContext(dispatcher) {
            val allCashbacksResult = repository.getAllCashbacks().onFailure {
                Log.e(TAG, it.message, it)
            }
            val allCashbacks = allCashbacksResult.getOrNull()
                ?: return@withContext allCashbacksResult
            val expiredCashbacks = allCashbacks.filter {
                when (val expirationDate = it.expirationDate) {
                    null -> false
                    else -> expirationDate < today
                }
            }
            return@withContext Result.success(expiredCashbacks)
        }
    }
}