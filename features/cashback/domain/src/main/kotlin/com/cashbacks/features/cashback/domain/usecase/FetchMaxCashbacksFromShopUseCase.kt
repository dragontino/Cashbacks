package com.cashbacks.features.cashback.domain.usecase

import android.util.Log
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.filterMaxCashbacks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface FetchMaxCashbacksFromShopUseCase {
    operator fun invoke(shopId: Long): Flow<Set<Cashback>>
}


internal class FetchMaxCashbacksFromShopUseCaseImpl(
    private val fetchAllCashbacksFromShop: FetchCashbacksFromShopUseCase
) : FetchMaxCashbacksFromShopUseCase {
    private companion object {
        const val TAG = "FetchMaxCashbacksFromShopUseCase"
    }

    override fun invoke(shopId: Long): Flow<Set<Cashback>> {
        return fetchAllCashbacksFromShop(shopId)
            .map { it.filterMaxCashbacks() }
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}