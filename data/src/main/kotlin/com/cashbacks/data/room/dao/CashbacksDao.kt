package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.BasicCashbackDB
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CashbackWithOwnerAndBankCardDB
import com.cashbacks.data.model.CashbackWithOwnersDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface CashbacksDao {
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
    suspend fun getCashbackById(id: Long): CashbackWithOwnersDB?


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
            ORDER BY cash.amount DESC
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
            ORDER BY cash.amount DESC
        """
    )
    fun fetchCashbacksFromShop(shopId: Long): Flow<List<BasicCashbackDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   cat.id AS category_id, 
                   cat.name AS category_name,
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
    fun fetchAllCashbacksFromCategories(): Flow<List<CashbackWithOwnerAndBankCardDB.Category>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   s.id AS shop_id,
                   s.categoryId AS shop_categoryId,
                   s.name AS shop_name,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Shops) AS s ON cash.shopId = s.id
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.shopId IS NOT NULL
        """
    )
    fun fetchAllCashbacksFromShops(): Flow<List<CashbackWithOwnerAndBankCardDB.Shop>>


    fun fetchAllCashbacks(): Flow<List<CashbackWithOwnerAndBankCardDB>> {
        val cashbacksFromCategories = fetchAllCashbacksFromCategories()
        val cashbacksFromShops = fetchAllCashbacksFromShops()
        return cashbacksFromCategories.combine(cashbacksFromShops) { cashbacksCategory, cashbacksShop ->
            (cashbacksCategory + cashbacksShop).sortedByDescending { it.amount }
        }
    }


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   cat.id AS category_id,
                   cat.name AS category_name,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT id, name FROM Categories) AS cat ON cash.categoryId = cat.id
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId IS NOT NULL
            AND (
                amount LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%' 
                OR expirationDate LIKE '%' || :query || '%' OR cash.comment LIKE '%' || :query || '%'
                OR card_name LIKE '%' || :query || '%' OR card_number LIKE '%' || :query || '%'
                OR card_paymentSystem LIKE '%' || :query || '%'
            )
        """
    )
    suspend fun searchCashbacksInCategories(query: String): List<CashbackWithOwnerAndBankCardDB.Category>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   s.id AS shop_id,
                   s.categoryId AS shop_categoryId,
                   s.name AS shop_name,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Shops) AS s ON cash.shopId = s.id
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.shopId IS NOT NULL
            AND (
                amount LIKE '%' || :query || '%' OR shop_name LIKE '%' || :query || '%' 
                OR expirationDate LIKE '%' || :query || '%' OR cash.comment LIKE '%' || :query || '%'
                OR card_name LIKE '%' || :query || '%' OR card_number LIKE '%' || :query || '%'
                OR card_paymentSystem LIKE '%' || :query || '%'
            )
        """
    )
    suspend fun searchCashbacksInShops(query: String): List<CashbackWithOwnerAndBankCardDB.Shop>


    @Transaction
    suspend fun searchCashbacks(query: String): List<CashbackWithOwnerAndBankCardDB> {
        return searchCashbacksInCategories(query) + searchCashbacksInShops(query)
    }


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
        """
    )
    suspend fun getAllCashbacks(): List<BasicCashbackDB>


    @Query("DELETE FROM Cashbacks WHERE id = :id")
    suspend fun deleteCashbackById(id: Long): Int


    @Query("DELETE FROM Cashbacks WHERE id IN (:ids)")
    suspend fun deleteCashbacksById(ids: List<Long>): Int
}