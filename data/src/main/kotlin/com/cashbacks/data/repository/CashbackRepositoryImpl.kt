package com.cashbacks.data.repository

import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.FullCashbackDB
import com.cashbacks.data.room.dao.CashbacksDao
import com.cashbacks.data.util.getDateRange
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.DeletionException
import com.cashbacks.domain.model.EntityException
import com.cashbacks.domain.model.EntityNotFoundException
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.InsertCashbackException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.UpdateException
import com.cashbacks.domain.repository.CashbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class CashbackRepositoryImpl(private val dao: CashbacksDao) : CashbackRepository {
    override suspend fun addCashbackToCategory(
        categoryId: Long,
        cashback: Cashback
    ): Result<Long> {
        when (val overflowingMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> {
                val cashbacksDB = CashbackDB(cashback, categoryId = categoryId)
                return addCashback(cashbacksDB)
            }

            else -> return Result.failure(InsertCashbackException(cashback, overflowingMonth))
        }
    }

    override suspend fun addCashbackToShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Long> {
        when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> {
                val cashbackDB = CashbackDB(cashback, shopId = shopId)
                return addCashback(cashbackDB)
            }

            else -> return Result.failure(InsertCashbackException(cashback, overflowMonth))
        }
    }


    private suspend fun addCashback(cashback: CashbackDB): Result<Long> {
        return dao.addCashback(cashback).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException(EntityException.Type.Cashback, cashback.id.toString())
                )

                else -> Result.success(id)
            }
        }
    }


    private suspend fun getOverflowingMonthOfCashback(cashback: Cashback): LocalDate? {
        val maxCashbacksNumber = cashback.bankCard.maxCashbacksNumber ?: return null
        val affectedDates = cashback.getDateRange().let { it.start..it.endInclusive }
        val cardCashbacksDates = dao
            .getAllCashbacksWithBankCard(cashback.bankCard.id)
            .map { it.getDateRange() }
            .filter { it.start in affectedDates || it.endInclusive in affectedDates }

        val cashbackCountInMonths = mutableMapOf<Int, Int>()
        for (dates in cardCashbacksDates) {
            var date = dates.start
            while (date < dates.endInclusive) {
                val monthNumberSince2000 = date.convertToMonthNumberSince2000()
                val monthCashbacksCount = cashbackCountInMonths
                    .getOrDefault(monthNumberSince2000, 0) + 1

                cashbackCountInMonths[monthNumberSince2000] = monthCashbacksCount

                if (monthCashbacksCount >= maxCashbacksNumber) {
                    return date
                }

                date = date.plus(value = 1, unit = DateTimeUnit.MONTH)
            }
        }

        return null
    }

    private fun LocalDate.convertToMonthNumberSince2000(): Int {
        return monthNumber + (year - 2000).coerceAtLeast(0) * 12
    }


    override suspend fun updateCashbackInCategory(
        categoryId: Long,
        cashback: Cashback
    ): Result<Unit> {
        when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> {
                val cashbackDB = CashbackDB(cashback, categoryId = categoryId)
                return updateCashback(cashbackDB)
            }

            else -> return Result.failure(InsertCashbackException(cashback, overflowMonth))
        }
    }


    override suspend fun updateCashbackInShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Unit> {
        when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> {
                val cashbackDB = CashbackDB(cashback, shopId = shopId)
                return updateCashback(cashbackDB)
            }

            else -> return Result.failure(InsertCashbackException(cashback, overflowMonth))
        }
    }


    private suspend fun updateCashback(cashback: CashbackDB): Result<Unit> {
        val updatedCount = dao.updateCashback(cashback)
        return when {
            updatedCount <= 0 ->
                Result.failure(UpdateException(EntityException.Type.Cashback, cashback.id.toString()))
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        val deletedCount = dao.deleteCashbackById(id = cashback.id)
        return when {
            deletedCount < 0 -> Result.failure(DeletionException(EntityException.Type.Cashback, cashback.amount))
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
                    type = EntityException.Type.Cashback,
                    name = cashbacks.joinToString()
                )
            )
        }
    }


    override suspend fun getCashbackById(id: Long): Result<FullCashback> {
        return when (val cashback = dao.getCashbackById(id)?.mapToDomainCashback()) {
            null -> Result.failure(EntityNotFoundException(EntityException.Type.Cashback, id.toString()))
            else -> Result.success(cashback)
        }
    }


    override fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashback>> {
        return dao.fetchCashbacksFromCategory(categoryId).map { list ->
            list.map { it.mapToDomainCashback() }
        }
    }


    override fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashback>> {
        return dao.fetchCashbacksFromShop(shopId).map { list ->
            list.map { it.mapToDomainCashback() }
        }
    }


    override fun fetchAllCashbacks(): Flow<List<FullCashback>> {
        return dao.fetchAllCashbacks().map {
            it.mapNotNull(FullCashbackDB::mapToDomainCashback)
        }
    }


    override suspend fun searchCashbacks(query: String): List<FullCashback> {
        return dao.searchCashbacks(query).mapNotNull { it.mapToDomainCashback() }
    }


    override suspend fun getAllCashbacks(): List<BasicCashback> {
        return dao.getAllCashbacks().map { it.mapToDomainCashback() }
    }
}