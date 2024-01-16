package com.cashbacks.data.repository

import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.repository.CashbackRepository

class CashbackRepositoryImpl(private val dao: CashbacksDao) : CashbackRepository {
    override suspend fun addCashbacksToCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, categoryId = categoryId) }
        return addCashbacks(cashbacksDB)
    }

    override suspend fun addCashbacksToShop(
        shopId: Long,
        cashbacks: List<Cashback>
    ): List<Result<Unit>> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, shopId = shopId) }
        return addCashbacks(cashbacksDB)
    }


    private suspend fun addCashbacks(cashbacks: List<CashbackDB>): List<Result<Unit>> {
        return dao.addCashbacks(cashbacks).mapIndexed { index, id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException("Не удалось добавить кэшбек ${cashbacks[index]} в базу данных")
                )
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun updateCashbacksInCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): Result<Unit> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, categoryId = categoryId) }
        return updateCashbacks(cashbacksDB)
    }

    override suspend fun updateCashbacksInShop(
        shopId: Long,
        cashbacks: List<Cashback>
    ): Result<Unit> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, shopId = shopId) }
        return updateCashbacks(cashbacksDB)
    }


    private suspend fun updateCashbacks(cashbacks: List<CashbackDB>): Result<Unit> {
        val updatedCount = dao.updateCashbacks(cashbacks)
        return when {
            updatedCount < cashbacks.size -> Result.failure(
                InsertionException("Не удалось обновить все кэшбеки")
            )

            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashbacksFromCategory(
        categoryId: Long,
        cashbacks: List<Cashback>
    ): Result<Unit> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, categoryId = categoryId) }
        return deleteCashbacks(cashbacksDB)
    }

    override suspend fun deleteCashbacksFromShop(
        shopId: Long,
        cashbacks: List<Cashback>
    ): Result<Unit> {
        val cashbacksDB = cashbacks.map { CashbackDB(it, shopId = shopId) }
        return deleteCashbacks(cashbacksDB)
    }


    private suspend fun deleteCashbacks(cashbacks: List<CashbackDB>): Result<Unit> {
        val deletedCount = dao.deleteCashbacks(cashbacks)
        return when {
            deletedCount < cashbacks.size -> Result.failure(Exception())
            else -> Result.success(Unit)
        }
    }


    override suspend fun getCashbackById(id: Long): Result<Cashback> {
        TODO("Not yet implemented")
    }
}