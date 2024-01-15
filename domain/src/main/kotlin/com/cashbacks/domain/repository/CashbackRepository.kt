package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Cashback

interface CashbackRepository {
    suspend fun addCashbacksToCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun addCashbacksToShop(shopId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun updateCashbacksInCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun updateCashbacksInShop(shopId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun deleteCashbacksFromCategory(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun deleteCashbacksFromShop(categoryId: Long, cashbacks: List<Cashback>): List<Result<Unit>>

    suspend fun getCashbackById(id: Long): Result<Cashback>
}