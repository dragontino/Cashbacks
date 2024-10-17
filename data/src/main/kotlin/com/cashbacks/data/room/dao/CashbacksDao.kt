package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.BasicCashbackDB
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.FullCashbackDB
import kotlinx.coroutines.flow.Flow

@Dao
interface CashbacksDao : BaseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addCashback(cashback: CashbackDB): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateCashback(cashback: CashbackDB): Int

    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   cat.id AS category_id, cat.name AS category_name,
                   s.id AS shop_id, s.categoryId AS shop_categoryId, s.name AS shop_name,
                   card.id AS card_id, card.name AS card_name, 
                   card.number AS card_number, card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            LEFT JOIN Categories AS cat ON cat.id = cash.categoryId
            LEFT JOIN Shops AS s ON s.id = cash.shopId
            INNER JOIN (SELECT id, name, number, paymentSystem FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.id = :id
        """,
    )
    suspend fun getCashbackById(id: Long): FullCashbackDB?


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
            ORDER BY cash.amount DESC
        """
    )
    fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashbackDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   cat.id AS category_id, cat.name AS category_name,
                   s.id AS shop_id, s.categoryId AS shop_categoryId, s.name AS shop_name,
                   card.id AS card_id, card.name AS card_name, card.number AS card_number, card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            LEFT JOIN Categories AS cat ON cash.categoryId = cat.id
            LEFT JOIN Shops AS s ON cash.shopId = s.id
            LEFT JOIN Cards AS card ON cash.bankCardId = card.id
            WHERE cash.categoryId IS NOT NULL OR cash.shopId IS NOT NULL
        """
    )
    fun fetchAllCashbacks(): Flow<List<FullCashbackDB>>


    @Transaction
    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   cat.id AS category_id, cat.name AS category_name,
                   s.id AS shop_id, s.categoryId AS shop_categoryId, s.name AS shop_name,
                   card.id AS card_id, card.name AS card_name, card.number AS card_number, card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            LEFT JOIN Categories AS cat ON cash.categoryId = cat.id
            LEFT JOIN Shops AS s ON cash.shopId = s.id
            LEFT JOIN Cards AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId IS NOT NULL OR cash.shopId IS NOT NULL
            AND (
                amount LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%' 
                OR shop_name LIKE '%' || :query || '%' OR expirationDate LIKE '%' || :query || '%' 
                OR cash.comment LIKE '%' || :query || '%' OR card_name LIKE '%' || :query || '%' 
                OR card_number LIKE '%' || :query || '%' OR card_paymentSystem LIKE '%' || :query || '%'
            )
        """
    )
    suspend fun searchCashbacks(query: String): List<FullCashbackDB>


    @Query("DELETE FROM Cashbacks WHERE id = :id")
    suspend fun deleteCashbackById(id: Long): Int


    @Query("DELETE FROM Cashbacks WHERE id IN (:ids)")
    suspend fun deleteCashbacksById(ids: List<Long>): Int
}