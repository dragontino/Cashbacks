package com.cashbacks.data.repository

import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CashbackWithOwnerAndBankCardDB
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithOwner
import com.cashbacks.domain.model.DeletionException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.UpdateException
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CashbackRepositoryImpl(private val dao: CashbacksDao) : CashbackRepository {
    override suspend fun addCashbackToCategory(categoryId: Long, cashback: Cashback): Result<Long> {
        val cashbacksDB = CashbackDB(cashback, categoryId = categoryId)
        return addCashback(cashbacksDB)
    }

    override suspend fun addCashbackToShop(shopId: Long, cashback: Cashback): Result<Long> {
        val cashbackDB = CashbackDB(cashback, shopId = shopId)
        return addCashback(cashbackDB)
    }


    private suspend fun addCashback(cashback: CashbackDB): Result<Long> {
        return dao.addCashback(cashback).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException(Cashback::class, cashback.id.toString())
                )

                else -> Result.success(id)
            }
        }
    }


    override suspend fun updateCashbackInCategory(categoryId: Long, cashback: Cashback): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, categoryId = categoryId)
        return updateCashback(cashbackDB)
    }


    override suspend fun updateCashbackInShop(shopId: Long, cashback: Cashback): Result<Unit> {
        val cashbackDB = CashbackDB(cashback, shopId = shopId)
        return updateCashback(cashbackDB)
    }


    private suspend fun updateCashback(cashback: CashbackDB): Result<Unit> {
        val updatedCount = dao.updateCashback(cashback)
        return when {
            updatedCount <= 0 ->
                Result.failure(UpdateException(Cashback::class, cashback.id.toString()))
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


    override suspend fun getCashbackById(id: Long): Result<CashbackWithOwner> {
        return when (val cashback = dao.getCashbackById(id)?.mapToCashback()) {
            null -> Result.failure(Exception("Не удалось извлечь данные!"))
            else -> Result.success(cashback)
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


    override fun fetchAllCashbacks(): Flow<List<CashbackWithOwner>> {
        return dao.fetchAllCashbacks().map {
            it.map(CashbackWithOwnerAndBankCardDB::mapToCashback)
        }
    }


    override suspend fun searchCashbacks(query: String): List<CashbackWithOwner> {
        return dao.searchCashbacks(query).map { it.mapToCashback() }
    }


    override suspend fun getAllCashbacks(): List<Cashback> {
        return dao.getAllCashbacks().map { it.mapToCashback() }
    }
}