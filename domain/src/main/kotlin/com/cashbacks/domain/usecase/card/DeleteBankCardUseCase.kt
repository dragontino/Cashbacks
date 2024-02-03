package com.cashbacks.domain.usecase.card

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteBankCardUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun deleteBankCard(
        bankCard: BankCard,
        errorMessage: (String) -> Unit = {}
    ) {
        withContext(dispatcher) {
            repository.deleteBankCard(bankCard)
                .apply { exceptionOrNull()?.message?.let(errorMessage) }
        }
    }
}