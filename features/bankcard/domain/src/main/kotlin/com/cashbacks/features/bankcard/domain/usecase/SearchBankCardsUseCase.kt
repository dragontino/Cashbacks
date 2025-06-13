package com.cashbacks.features.bankcard.domain.usecase

import android.util.Log
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SearchBankCardsUseCase {
    suspend operator fun invoke(query: String): Result<List<PrimaryBankCard>>
}


internal class SearchBankCardsUseCaseImpl(
    private val repository: BankCardRepository,
    private val dispatcher: CoroutineDispatcher
) : SearchBankCardsUseCase {
    private companion object {
        const val TAG = "SearchBankCardsUseCase"
    }

    override suspend fun invoke(query: String): Result<List<PrimaryBankCard>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> Result.success(listOf())
                else -> repository.searchBankCards(query).onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }
}