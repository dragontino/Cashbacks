package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Cashback
import kotlinx.coroutines.flow.Flow

interface CashbackRepository {
    suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Unit>

    suspend fun addCashbackToShop(shopId: Long, cashback: Cashback): Result<Unit>

    suspend fun updateCashback(cashback: Cashback): Result<Unit>

    suspend fun deleteCashback(cashback: Cashback): Result<Unit>

    suspend fun deleteCashbacks(cashbacks: List<Cashback>): Result<Unit>

    suspend fun getCashbackById(id: Long): Result<Cashback>

    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<Cashback>>

    fun fetchCashbacksFromShop(shopId: Long): Flow<List<Cashback>>

    fun fetchAllCashbacks(): Flow<List<Pair<Pair<String, String>, Cashback>>>

    suspend fun getAllCashbacks(): List<Cashback>

    suspend fun searchCashbacks(query: String): List<Pair<Pair<String, String>, Cashback>>
}