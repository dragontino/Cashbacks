package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface DeleteBankCardUseCase {
    suspend operator fun invoke(bankCard: BasicBankCard): Result<Unit>
}


internal class DeleteBankCardUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : DeleteBankCardUseCase {
    private companion object {
        const val TAG = "DeleteBankCardUseCase"
    }

    override suspend fun invoke(bankCard: BasicBankCard): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteBankCard(bankCard).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}