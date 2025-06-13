package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

sealed interface FetchBankCardByIdUseCase {
    suspend operator fun invoke(id: Long): Flow<FullBankCard>
}


internal class FetchBankCardByIdUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchBankCardByIdUseCase {
    private companion object {
        const val TAG = "FetchBankCardsByIdUseCase"
    }


    override suspend fun invoke(id: Long): Flow<FullBankCard> {
        return repository.fetchBankCardById(id)
            .flowOn(dispatcher)
            .catch { throwable ->
                Log.e(TAG, throwable.message, throwable)
                throw throwable
            }
    }
}