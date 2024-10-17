package com.cashbacks.domain.usecase.cards

import android.util.Log
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class GetBankCardUseCase(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "GetBankCardUseCase"
    }

    suspend fun getBankCardById(id: Long): Result<FullBankCard> = withContext(dispatcher) {
        return@withContext repository.getBankCardById(id).onFailure { throwable ->
            Log.e(TAG, throwable.message, throwable)
        }
    }


    suspend fun fetchBankCardById(
        id: Long,
        onFailure: (Throwable) -> Unit = {}
    ): Flow<FullBankCard> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }

        return repository.fetchBankCardById(id).flowOn(handler + dispatcher)
    }
}