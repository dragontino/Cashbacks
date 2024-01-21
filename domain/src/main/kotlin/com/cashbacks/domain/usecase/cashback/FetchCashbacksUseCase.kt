package com.cashbacks.domain.usecase.cashback

import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.flow.Flow

class FetchCashbacksUseCase(
    private val repository: CashbackRepository
) {
    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<Cashback>> {
        return repository.fetchCashbacksFromCategory(categoryId)
    }

    fun fetchCashbacksFromShop(shopId: Long): Flow<List<Cashback>> {
        return repository.fetchCashbacksFromShop(shopId)
    }
}