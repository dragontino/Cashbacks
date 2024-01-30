package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BankCard
import kotlinx.coroutines.flow.Flow

interface BankCardRepository {
    suspend fun addBankCard(bankCard: BankCard)

    suspend fun updateBankCard(bankCard: BankCard)

    fun fetchBankCards(): Flow<List<BankCard>>

    suspend fun getBankCardById(id: Long): Result<BankCard>

    suspend fun deleteBankCard(card: BankCard): Result<Unit>
}