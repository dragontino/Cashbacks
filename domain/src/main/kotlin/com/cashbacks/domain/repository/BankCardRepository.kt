package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.PrimaryBankCard
import kotlinx.coroutines.flow.Flow

interface BankCardRepository {
    suspend fun addBankCard(bankCard: FullBankCard): Result<Long>

    suspend fun updateBankCard(bankCard: FullBankCard): Result<Unit>

    fun fetchBankCards(): Flow<List<PrimaryBankCard>>

    suspend fun searchBankCards(query: String): List<PrimaryBankCard>

    suspend fun getBankCardById(id: Long): Result<FullBankCard>

    suspend fun fetchBankCardById(id: Long): Flow<FullBankCard>

    suspend fun deleteBankCard(card: BasicBankCard): Result<Unit>
}