package com.cashbacks.features.bankcard.domain.repo

import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import kotlinx.coroutines.flow.Flow

interface BankCardRepository {
    suspend fun addBankCard(bankCard: FullBankCard): Result<Long>

    suspend fun updateBankCard(bankCard: FullBankCard): Result<Unit>

    fun fetchAllBankCards(): Flow<List<PrimaryBankCard>>

    suspend fun searchBankCards(query: String): Result<List<PrimaryBankCard>>

    suspend fun getBankCardById(id: Long): Result<FullBankCard>

    suspend fun fetchBankCardById(id: Long): Flow<FullBankCard>

    suspend fun deleteBankCard(card: BasicBankCard): Result<Unit>
}