package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EditBankCardUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addBankCard(bankCard: BankCard) {
        withContext(dispatcher) {
            repository.addBankCard(bankCard)
        }
    }

    suspend fun updateBankCard(bankCard: BankCard) {
        withContext(dispatcher) {
            repository.updateBankCard(bankCard)
        }
    }

    suspend fun getBankCardById(
        id: Long,
        errorMessage: (String) -> Unit = {}
    ): BankCard? = withContext(dispatcher) {
        repository.getBankCardById(id)
            .also { it.exceptionOrNull()?.message?.let(errorMessage) }
            .getOrNull()
    }

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