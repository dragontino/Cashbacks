package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CashbackWithBankCardDB
import com.cashbacks.data.room.PaymentSystemConverter
import kotlinx.coroutines.flow.Flow

@Dao
interface CashbacksDao : CardsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCashback(cashback: CashbackDB): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateCashback(cashback: CashbackDB): Int

    @TypeConverters(PaymentSystemConverter::class)
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
            WHERE cash.id = :id
        """,
    )
    suspend fun getCashbackById(id: Long): CashbackWithBankCardDB?


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
            WHERE cash.categoryId = :categoryId
        """,
    )
    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<CashbackWithBankCardDB>>


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
            WHERE cash.shopId = :shopId
        """,
    )
    fun fetchCashbacksFromShop(shopId: Long): Flow<List<CashbackWithBankCardDB>>

    @Delete
    suspend fun deleteCashbacks(cashback: CashbackDB): Int
}