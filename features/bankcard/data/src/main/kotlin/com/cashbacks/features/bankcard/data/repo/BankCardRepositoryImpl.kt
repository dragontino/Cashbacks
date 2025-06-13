package com.cashbacks.features.bankcard.data.repo

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.core.database.dao.CardsDao
import com.cashbacks.core.database.utils.mapToBankCard
import com.cashbacks.core.database.utils.mapToDomainBankCard
import com.cashbacks.core.database.utils.mapToEntity
import com.cashbacks.features.bankcard.data.resources.CardDeletionException
import com.cashbacks.features.bankcard.data.resources.CardInsertionException
import com.cashbacks.features.bankcard.data.resources.CardNotFoundException
import com.cashbacks.features.bankcard.data.resources.CardUpdateException
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BankCardRepositoryImpl(
    private val dao: CardsDao,
    private val context: Context
) : BankCardRepository {

    override suspend fun addBankCard(bankCard: FullBankCard): Result<Long> {
        return when (val id = dao.addBankCard(bankCard.mapToEntity())) {
            null -> Result.failure(CardInsertionException(bankCard.name).toException(context))
            else -> Result.success(id)
        }
    }


    override suspend fun updateBankCard(bankCard: FullBankCard): Result<Unit> {
        val updatedRowsCount = dao.updateBankCard(bankCard.mapToEntity())
        return when {
            updatedRowsCount < 1 -> Result.failure(
                CardUpdateException(bankCard.name).toException(context)
            )
            else -> Result.success(Unit)
        }
    }


    override fun fetchAllBankCards(): Flow<List<PrimaryBankCard>> {
        return dao.fetchAllBankCards().map { list ->
            list.map { it.mapToDomainBankCard() }
        }
    }


    override suspend fun searchBankCards(query: String): Result<List<PrimaryBankCard>> {
        val response = dao.searchBankCards(query)
        return response.map { it.mapToDomainBankCard() }.let { Result.success(it) }
    }


    override suspend fun getBankCardById(id: Long): Result<FullBankCard> {
        return dao.getBankCardById(id)
            ?.let { Result.success(it.mapToBankCard()) }
            ?: Result.failure(CardNotFoundException(id).toException(context))
    }


    override suspend fun fetchBankCardById(id: Long): Flow<FullBankCard> {
        return dao.fetchBankCardById(id).map { it.mapToBankCard() }
    }


    override suspend fun deleteBankCard(card: BasicBankCard): Result<Unit> {
        return when {
            dao.deleteBankCardById(card.id) == 1 -> Result.success(Unit)
            else -> Result.failure(CardDeletionException(card.name).toException(context))
        }
    }
}