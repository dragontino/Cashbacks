package com.cashbacks.features.cashback.data.repo

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.core.database.dao.CashbacksDao
import com.cashbacks.core.database.entity.CashbackEntity
import com.cashbacks.core.database.entity.FullCashbackEntity
import com.cashbacks.core.database.utils.getDateRange
import com.cashbacks.core.database.utils.mapList
import com.cashbacks.core.database.utils.mapToDomainCashback
import com.cashbacks.core.database.utils.mapToEntity
import com.cashbacks.features.cashback.data.resources.CashbackNotFoundException
import com.cashbacks.features.cashback.data.resources.CashbackOverflowException
import com.cashbacks.features.cashback.data.resources.DeletionException
import com.cashbacks.features.cashback.data.resources.InsertionException
import com.cashbacks.features.cashback.data.resources.UpdateException
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.displayableAmount
import com.cashbacks.features.cashback.domain.utils.getDateRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal class CashbackRepositoryImpl(
    private val dao: CashbacksDao,
    private val context: Context
) : CashbackRepository {

    override suspend fun addCashbackToCategory(
        categoryId: Long,
        cashback: Cashback
    ): Result<Long> {
        return when (val overflowingMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> addCashback(cashback.mapToEntity(categoryId = categoryId))
            else -> Result.failure(
                CashbackOverflowException(cashback, overflowingMonth).toException(context)
            )
        }
    }

    override suspend fun addCashbackToShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Long> {
        return when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> addCashback(cashback.mapToEntity(shopId = shopId))
            else -> Result.failure(
                CashbackOverflowException(cashback, overflowMonth).toException(context)
            )
        }
    }


    private suspend fun addCashback(cashback: CashbackEntity): Result<Long> {
        return dao.addCashback(cashback).let { id ->
            when {
                id < 0 -> Result.failure(InsertionException(cashback.id).toException(context))
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
        return when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> updateCashback(cashback.mapToEntity(categoryId = categoryId))
            else -> Result.failure(
                CashbackOverflowException(cashback, overflowMonth).toException(context)
            )
        }
    }


    override suspend fun updateCashbackInShop(
        shopId: Long,
        cashback: Cashback
    ): Result<Unit> {
        return when (val overflowMonth = getOverflowingMonthOfCashback(cashback)) {
            null -> updateCashback(cashback.mapToEntity(shopId = shopId))
            else -> Result.failure(
                CashbackOverflowException(cashback, overflowMonth).toException(context)
            )
        }
    }


    private suspend fun updateCashback(cashback: CashbackEntity): Result<Unit> {
        val updatedCount = dao.updateCashback(cashback)
        return when {
            updatedCount <= 0 -> Result.failure(UpdateException(cashback.id).toException(context))
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashback(cashback: Cashback): Result<Unit> {
        val deletedCount = dao.deleteCashbackById(id = cashback.id)
        return when {
            deletedCount < 0 -> Result.failure(
                DeletionException(cashback.displayableAmount).toException(context)
            )
            else -> Result.success(Unit)
        }
    }


    override suspend fun deleteCashbacks(cashbacks: List<Cashback>): Result<Int> {
        if (cashbacks.isEmpty()) return Result.success(0)
        val deletedCount = dao.deleteCashbacksById(cashbacks.map { it.id })
        return when {
            deletedCount >= cashbacks.size / 2 -> Result.success(deletedCount)
            else -> Result.failure(
                DeletionException(cashbacks.joinToString { it.displayableAmount }).toException(context)
            )
        }
    }


    override suspend fun getCashbackById(id: Long): Result<FullCashback> {
        return when (val cashback = dao.getFullCashbackById(id)?.mapToDomainCashback()) {
            null -> Result.failure(CashbackNotFoundException(id).toException(context))
            else -> Result.success(cashback)
        }
    }


    override fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashback>> {
        return dao.fetchCashbacksFromCategory(categoryId).mapList { it.mapToDomainCashback() }
    }


    override fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashback>> {
        return dao.fetchCashbacksFromShop(shopId).mapList { it.mapToDomainCashback() }
    }


    override fun fetchAllCashbacks(): Flow<List<FullCashback>> {
        return dao.fetchAllCashbacks().map {
            it.mapNotNull(FullCashbackEntity::mapToDomainCashback)
        }
    }


    override suspend fun getAllCashbacksFromCategory(categoryId: Long): Result<List<Cashback>> {
        return fetchCashbacksFromCategory(categoryId).firstOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception())
    }


    override suspend fun getAllCashbacksFromShop(shopId: Long): Result<List<Cashback>> {
        return fetchCashbacksFromShop(shopId).firstOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(Exception())
    }


    override suspend fun getAllCashbacks(): Result<List<Cashback>> = runCatching {
        dao.getAllCashbacks().map { it.mapToDomainCashback() }
    }


    override suspend fun searchCashbacks(query: String): Result<List<FullCashback>> = runCatching {
        dao.searchCashbacks(query).mapNotNull { it.mapToDomainCashback() }
    }
}