package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.FullCashback
import kotlinx.coroutines.flow.Flow

interface CashbackRepository {
    suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Long>

    suspend fun addCashbackToShop(shopId: Long, cashback: Cashback): Result<Long>

    suspend fun updateCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit>

    suspend fun updateCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit>

    suspend fun deleteCashback(cashback: Cashback): Result<Unit>

    suspend fun deleteCashbacks(cashbacks: List<Cashback>): Result<Unit>

    suspend fun getCashbackById(id: Long): Result<FullCashback>

    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashback>>

    fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashback>>

    fun fetchAllCashbacks(): Flow<List<FullCashback>>

    suspend fun getAllCashbacks(): List<Cashback>

    suspend fun searchCashbacks(query: String): List<FullCashback>
}