package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface UpdateBankCardUseCase {
    suspend operator fun invoke(card: FullBankCard): Result<Unit>
}


internal class UpdateBankCardUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateBankCardUseCase {
    private companion object {
        const val TAG = "UpdateBankCardUseCase"
    }

    override suspend fun invoke(card: FullBankCard): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateBankCard(card).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}