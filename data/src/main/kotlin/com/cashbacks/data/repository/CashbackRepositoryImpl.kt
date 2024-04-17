package com.cashbacks.data.repository

import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.ParentCashbackWithBankCardDB
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithParent
import com.cashbacks.domain.model.DeletionException
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


    override suspend fun updateCashback(cashback: Cashback): Result<Unit> {
        val updatedCount = dao.updateCashbackById(
            id = cashback.id,
            bankCardId = cashback.bankCard.id,
            amount = cashback.amount.toDoubleOrNull() ?: -1.0,
            expirationDate = cashback.expirationDate,
            comment = cashback.comment
        )
        return when {
            updatedCount < 0 -> Result.failure(
                InsertionException("Не удалось обновить кэшбек")
            )
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        val deletedCount = dao.deleteCashbackById(id = cashback.id)
        return when {
            deletedCount < 0 -> Result.failure(DeletionException(Cashback::class, cashback.amount))
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashbacks(cashbacks: List<Cashback>): Result<Unit> {
        if (cashbacks.isEmpty()) return Result.success(Unit)
        val deletedCount = dao.deleteCashbacksById(cashbacks.map { it.id })
        return when {
            deletedCount >= cashbacks.size / 2 -> Result.success(Unit)
            else -> Result.failure(
                DeletionException(
                    type = Cashback::class,
                    name = cashbacks.joinToString { it.expirationDate!! }
                )
            )
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


    override fun fetchAllCashbacks(): Flow<List<CashbackWithParent>> {
        return dao.fetchAllCashbacks().map {
            it.map(ParentCashbackWithBankCardDB::mapToCashback)
        }
    }


    override suspend fun searchCashbacks(query: String): List<CashbackWithParent> {
        return dao.searchCashbacks(query).map { it.mapToCashback() }
    }


    override suspend fun getAllCashbacks(): List<Cashback> {
        return dao.getAllCashbacks().map { it.mapToCashback() }
    }
}