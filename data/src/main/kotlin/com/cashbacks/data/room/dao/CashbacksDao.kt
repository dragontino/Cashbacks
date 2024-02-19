package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.cashbacks.data.model.BasicCashbackDB
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CashbackWithBankCardDB
import com.cashbacks.data.model.CashbackWithParentAndBankCardDB
import com.cashbacks.data.room.PaymentSystemConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface CashbacksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCashback(cashback: CashbackDB): Long

    @Query(
        """
            UPDATE Cashbacks 
            SET bankCardId = :bankCardId,
                amount = :amount,
                expirationDate = :expirationDate,
                comment = :comment
            WHERE id = :id
        """
    )
    suspend fun updateCashbackById(
        id: Long,
        bankCardId: Long,
        amount: Double,
        expirationDate: String?,
        comment: String
    ): Int

    @TypeConverters(PaymentSystemConverter::class)
    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem,
                   card.holder AS card_holder,
                   card.validityPeriod AS card_validityPeriod,
                   card.cvv AS card_cvv,
                   card.pin AS card_pin,
                   card.comment AS card_comment
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
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
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId = :categoryId
        """,
    )
    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashbackDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.shopId = :shopId
            ORDER BY cash.amount
        """
    )
    fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashbackDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   "Category" AS parentType,
                   cat.name AS parentName,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT id, name FROM Categories) AS cat ON cash.categoryId = cat.id
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId IS NOT NULL
        """
    )
    fun fetchAllCashbacksFromCategories(): Flow<List<CashbackWithParentAndBankCardDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   "Shop" AS parentType,
                   s.name AS parentName,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT id, name FROM Shops) AS s ON cash.shopId = s.id
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.shopId IS NOT NULL
        """
    )
    fun fetchAllCashbacksFromShops(): Flow<List<CashbackWithParentAndBankCardDB>>


    fun fetchAllCashbacks(): Flow<List<CashbackWithParentAndBankCardDB>> {
        val cashbacksFromCategories = fetchAllCashbacksFromCategories()
        val cashbacksFromShops = fetchAllCashbacksFromShops()
        return cashbacksFromCategories.combine(cashbacksFromShops) { cashbacksCategory, cashbacksShop ->
            (cashbacksCategory + cashbacksShop).sortedByDescending { it.amount }
        }
    }


    @Query("DELETE FROM Cashbacks WHERE id = :id")
    suspend fun deleteCashbackById(id: Long): Int
}