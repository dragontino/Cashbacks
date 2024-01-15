package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetCashbackUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun getCashbackById(id: Long): Result<Cashback> {
        return withContext(dispatcher) {
            repository.getCashbackById(id)
        }
    }
}