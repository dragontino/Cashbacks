package com.cashbacks.data.repository

import com.cashbacks.data.model.BankCardDB
import com.cashbacks.data.room.dao.CardsDao
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.PrimaryBankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankCardRepositoryImpl(private val dao: CardsDao) : BankCardRepository {
    override suspend fun addBankCard(bankCard: FullBankCard): Result<Long> {
        return when (val id = dao.addBankCard(BankCardDB(bankCard))) {
            null -> Result.failure(Exception())
            else -> Result.success(id)
        }
    }


    override suspend fun updateBankCard(bankCard: FullBankCard): Result<Unit> {
        val updatedRowsCount = dao.updateBankCard(BankCardDB(bankCard))
        return when {
            updatedRowsCount < 1 -> Result.failure(Exception())
            else -> Result.success(Unit)
        }
    }


    override fun fetchBankCards(): Flow<List<PrimaryBankCard>> {
        return dao.fetchBankCards().map { list ->
            list.map { it.mapToDomainBankCard() }
        }
    }


    override suspend fun searchBankCards(query: String): List<PrimaryBankCard> {
        return dao.searchBankCards(query).map { it.mapToDomainBankCard() }
    }


    override suspend fun getBankCardById(id: Long): Result<FullBankCard> {
        return dao.getBankCardById(id)
            ?.let { Result.success(it.mapToBankCard()) }
            ?: Result.failure(Exception())
    }


    override suspend fun fetchBankCardById(id: Long): Flow<FullBankCard> {
        return dao.fetchBankCardById(id).map { it.mapToBankCard() }
    }


    override suspend fun deleteBankCard(card: BasicBankCard): Result<Unit> {
        val success = dao.deleteBankCardById(card.id) == 1
        return when {
            success -> Result.success(Unit)
            else -> Result.failure(Exception())
        }
    }
}