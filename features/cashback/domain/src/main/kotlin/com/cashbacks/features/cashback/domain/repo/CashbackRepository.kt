package com.cashbacks.features.cashback.domain.repo

import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.FullCashback
import kotlinx.coroutines.flow.Flow

interface CashbackRepository {
    suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Long>

    suspend fun addCashbackToShop(shopId: Long, cashback: Cashback): Result<Long>

    suspend fun updateCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit>

    suspend fun updateCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit>

    suspend fun deleteCashback(cashback: Cashback): Result<Unit>

    suspend fun deleteCashbacks(cashbacks: List<Cashback>): Result<Int>

    suspend fun getCashbackById(id: Long): Result<FullCashback>

    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashback>>

    fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashback>>

    fun fetchAllCashbacks(): Flow<List<FullCashback>>

    suspend fun getAllCashbacksFromCategory(categoryId: Long): Result<List<Cashback>>

    suspend fun getAllCashbacksFromShop(shopId: Long): Result<List<Cashback>>

    suspend fun getAllCashbacks(): Result<List<Cashback>>

    suspend fun searchCashbacks(query: String): Result<List<FullCashback>>
}