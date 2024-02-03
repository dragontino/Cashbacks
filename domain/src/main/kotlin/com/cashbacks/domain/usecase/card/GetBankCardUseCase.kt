package com.cashbacks.domain.usecase.card

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetBankCardUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun getBankCardById(
        id: Long,
        errorMessage: (String) -> Unit = {}
    ): BankCard? = withContext(dispatcher) {
        repository.getBankCardById(id)
            .also { it.exceptionOrNull()?.message?.let(errorMessage) }
            .getOrNull()
    }
}