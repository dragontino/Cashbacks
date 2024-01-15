package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CashbackWithBankCardDB

@Dao
interface CashbacksDao : CardsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCashbacks(cashbacks: List<CashbackDB>): List<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateCashbacks(cashbacks: List<CashbackDB>): Int

    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (
                SELECT id, name, number, paymentSystem FROM Cards
            ) AS card ON card.id = cash.bankCardId
            WHERE id = :id
        """,
    )
    suspend fun getCashbackById(id: Long): CashbackWithBankCardDB
}