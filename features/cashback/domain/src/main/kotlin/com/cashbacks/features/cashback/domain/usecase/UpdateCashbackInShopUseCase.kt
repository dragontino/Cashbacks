package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UpdateCashbackInShopUseCase {
    suspend operator fun invoke(shopId: Long, cashback: Cashback): Result<Unit>
}


internal class UpdateCashbackInShopUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : UpdateCashbackInShopUseCase {
    private companion object {
        const val TAG = "UpdateCashbackInShopUseCase"
    }

    override suspend fun invoke(shopId: Long, cashback: Cashback): Result<Unit> {
        return withContext(dispatcher) {
            repository.updateCashbackInShop(shopId, cashback).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}