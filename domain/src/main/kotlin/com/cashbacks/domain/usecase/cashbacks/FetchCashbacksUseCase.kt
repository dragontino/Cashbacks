package com.cashbacks.domain.usecase.cashbacks

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithParent
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FetchCashbacksUseCase(
    private val repository: CashbackRepository,
    private val dispatcher: CoroutineDispatcher
) {
    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<Cashback>> {
        return repository.fetchCashbacksFromCategory(categoryId).flowOn(dispatcher)
    }

    fun fetchCashbacksFromShop(shopId: Long): Flow<List<Cashback>> {
        return repository.fetchCashbacksFromShop(shopId).flowOn(dispatcher)
    }

    fun fetchAllCashbacks(): Flow<List<CashbackWithParent>> {
        return repository.fetchAllCashbacks().flowOn(dispatcher)
    }
}