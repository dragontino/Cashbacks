package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchCashbacksFromShopUseCase {
    operator fun invoke(shopId: Long): Flow<List<BasicCashback>>
}


internal class FetchCashbacksFromShopUseCaseImpl(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchCashbacksFromShopUseCase {
    private companion object {
        const val TAG = "FetchCashbacksFromShopUseCase"
    }

    override fun invoke(shopId: Long): Flow<List<BasicCashback>> {
        return repository.fetchCashbacksFromShop(shopId)
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}