package com.cashbacks.domain.usecase.cards

import android.util.Log
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteBankCardUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "DeleteBankCardUseCase"
    }

    suspend fun deleteBankCard(bankCard: BasicBankCard) = withContext(dispatcher) {
        repository.deleteBankCard(bankCard).onFailure {
            Log.e(TAG, it.message, it)
        }
    }
}