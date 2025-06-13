package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

sealed interface GetBankCardUseCase {
    suspend operator fun invoke(id: Long): Result<FullBankCard>
}


internal class GetBankCardUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : GetBankCardUseCase {
    private companion object {
        const val TAG = "GetBankCardUseCase"
    }

    override suspend fun invoke(id: Long): Result<FullBankCard> {
        return withContext(dispatcher) {
            repository.getBankCardById(id).onFailure { throwable ->
                Log.e(TAG, throwable.message, throwable)
            }
        }
    }
}