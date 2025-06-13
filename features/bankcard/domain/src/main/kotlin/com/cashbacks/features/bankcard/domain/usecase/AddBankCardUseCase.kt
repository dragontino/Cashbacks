package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface AddBankCardUseCase {
    suspend operator fun invoke(card: FullBankCard): Result<Long>
}

internal class AddBankCardUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : AddBankCardUseCase {
    private companion object {
        const val TAG = "AddBankCardUseCase"
    }

    override suspend fun invoke(card: FullBankCard): Result<Long> {
        return withContext(dispatcher) {
            repository.addBankCard(card).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}