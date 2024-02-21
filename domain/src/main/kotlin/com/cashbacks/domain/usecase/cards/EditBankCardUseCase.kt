package com.cashbacks.domain.usecase.cards

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
}