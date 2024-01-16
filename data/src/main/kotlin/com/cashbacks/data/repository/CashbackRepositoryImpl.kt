package com.cashbacks.data.repository

import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CashbackRepositoryImpl(private val dao: CashbacksDao) : CashbackRepository {
    override suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        val cashbacksDB = CashbackDB(cashback, categoryId = categoryId)
        return addCashback(cashbacksDB)
    }

    override suspend fun addCashbackToShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, shopId = shopId)
        return addCashback(cashbackDB)
    }


    private suspend fun addCashback(cashback: CashbackDB): Result<Unit> {
        return dao.addCashback(cashback).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException("Не удалось добавить кэшбек $cashback в базу данных")
                )

                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun updateCashbackInCategory(
        categoryId: Long,
        cashback: Cashback
    ): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, categoryId = categoryId)
        return updateCashback(cashbackDB)
    }

    override suspend fun updateCashbackInShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, shopId = shopId)
        return updateCashback(cashbackDB)
    }


    private suspend fun updateCashback(cashback: CashbackDB): Result<Unit> {
        val updatedCount = dao.updateCashback(cashback)
        return when {
            updatedCount < 0 -> Result.failure(
                InsertionException("Не удалось обновить кэшбек")
            )
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashbackFromCategory(
        categoryId: Long,
        cashback: Cashback
    ): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, categoryId = categoryId)
        return deleteCashback(cashbackDB)
    }

    override suspend fun deleteCashbackFromShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, shopId = shopId)
        return deleteCashback(cashbackDB)
    }


    private suspend fun deleteCashback(cashback: CashbackDB): Result<Unit> {
        val deletedCount = dao.deleteCashbacks(cashback)
        return when {
            deletedCount < 0 -> Result.failure(Exception())
            else -> Result.success(Unit)
        }
    }


    override suspend fun getCashbackById(id: Long): Result<Cashback> {
        return dao.getCashbackById(id).let {
            when (it) {
                null -> Result.failure(Exception())
                else -> Result.success(it.mapToCashback())
            }
        }
    }


    override fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<Cashback>> {
        return dao.fetchCashbacksFromCategory(categoryId).map { list ->
            list.map { it.mapToCashback() }
        }
    }


    override fun fetchCashbacksFromShop(shopId: Long): Flow<List<Cashback>> {
        return dao.fetchCashbacksFromShop(shopId).map { list ->
            list.map { it.mapToCashback() }
        }
    }
}