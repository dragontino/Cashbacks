package com.cashbacks.data.repository

import com.cashbacks.data.model.BankCardDB
import com.cashbacks.data.room.dao.CardsDao
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.repository.BankCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankCardRepositoryImpl(private val dao: CardsDao) : BankCardRepository {
    override suspend fun addBankCard(bankCard: BankCard) {
        dao.addBankCard(BankCardDB(bankCard))
    }

    override suspend fun updateBankCard(bankCard: BankCard) {
        dao.updateBankCard(BankCardDB(bankCard))
    }

    override fun fetchBankCards(): Flow<List<BankCard>> {
        return dao.fetchBankCards().map { list ->
            list.map { it.mapToBankCard() }
        }
    }

    override suspend fun getBankCardById(id: Long): Result<BankCard> {
        return dao.getBankCardById(id)
            ?.let { Result.success(it.mapToBankCard()) }
            ?: Result.failure(Exception())
    }

    override suspend fun deleteBankCard(card: BankCard): Result<Unit> {
        val success = dao.deleteBankCard(BankCardDB(card)) == 1
        return if (success) Result.success(Unit) else Result.failure(Exception())
    }

}