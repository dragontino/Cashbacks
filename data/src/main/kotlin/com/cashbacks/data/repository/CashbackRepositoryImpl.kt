package com.cashbacks.data.repository

import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.repository.CashbackRepository

class CashbackRepositoryImpl(private val dao: CashbacksDao) : CashbackRepository {
    override suspend fun addCashbacksToCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun addCashbacksToShop(
        shopId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateCashbacksInCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateCashbacksInShop(
        shopId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCashbacksFromCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCashbacksFromShop(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCashbackById(id: Long): Result<Cashback> {
        TODO("Not yet implemented")
    }
}