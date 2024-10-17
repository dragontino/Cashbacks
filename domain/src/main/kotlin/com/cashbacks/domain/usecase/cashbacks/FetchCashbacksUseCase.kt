package com.cashbacks.domain.usecase.cashbacks

import android.util.Log
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "FetchCashbacksUseCase"
    }

    fun fetchCashbacksFromCategory(
        categoryId: Long,
        onFailure: (Throwable) -> Unit = {}
    ): Flow<List<BasicCashback>> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }
        return repository.fetchCashbacksFromCategory(categoryId).flowOn(handler + dispatcher)
    }

    fun fetchCashbacksFromShop(
        shopId: Long,
        onFailure: (Throwable) -> Unit = {}
    ): Flow<List<BasicCashback>> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }
        return repository.fetchCashbacksFromShop(shopId).flowOn(handler + dispatcher)
    }

    fun fetchAllCashbacks(onFailure: (Throwable) -> Unit = {}): Flow<List<FullCashback>> {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
            onFailure(throwable)
        }
        return repository.fetchAllCashbacks().flowOn(handler + dispatcher)
    }
}