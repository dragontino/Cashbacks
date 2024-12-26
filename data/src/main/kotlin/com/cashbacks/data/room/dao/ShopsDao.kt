package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.BasicShopDB
import com.cashbacks.data.model.CategoryShopDB
import com.cashbacks.data.model.ShopDB
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao : BaseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addShop(shop: ShopDB): Long

    @Query("SELECT COUNT(name) FROM Shops WHERE name = :name")
    suspend fun countShopsWithSameName(name: String): Int

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateShop(shop: ShopDB): Int

    @Delete
    suspend fun deleteShop(shop: ShopDB): Int

    @Query("DELETE FROM Shops WHERE id = :id")
    suspend fun deleteShopById(id: Long): Int

    @Query(
        """
            SELECT s.id, s.name,
                   cash.id AS cashback_id, cash.amount AS cashback_amount, 
                   cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
                   card.id AS cashback_card_id, card.name AS cashback_card_name,
                   card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
                   card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
            FROM Shops AS s
            LEFT JOIN Cashbacks AS cash 
            ON s.id = cash.shopId AND cash.amount = (
                SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
            )
            LEFT JOIN Cards AS card ON cash.bankCardId = card.id
            WHERE s.categoryId = :categoryId
            ORDER BY s.name ASC
        """
    )
    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<BasicShopDB>>


    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Shops AS s
        LEFT JOIN (SELECT id, name FROM Categories) AS cat ON s.categoryId = cat.id
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash
        ON s.id = cash.shopId AND cash.amount = (
            SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        ORDER BY s.name ASC
        """
    )
    fun fetchAllShops(): Flow<List<CategoryShopDB>>

    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Shops AS s
        LEFT JOIN (SELECT id, name FROM Categories) AS cat ON s.categoryId = cat.id
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON s.id = cash.shopId AND cash.amount = (
            SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE cash.id IS NOT NULL
        ORDER BY s.name ASC
        """
    )
    fun fetchShopsWithCashback(): Flow<List<CategoryShopDB>>

    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Shops AS s
        LEFT JOIN (SELECT id, name FROM Categories) AS cat ON s.categoryId = cat.id
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON s.id = cash.shopId AND cash.amount = (
            SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE s.name LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%'
        ORDER BY s.name ASC
        """
    )
    suspend fun searchAllShops(query: String): List<CategoryShopDB>

    @Transaction
    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Shops AS s
        LEFT JOIN Categories AS cat ON s.categoryId = cat.id
        LEFT JOIN Cashbacks AS cash ON 
            s.id = cash.shopId AND 
            cash.amount = (SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id)
        LEFT JOIN (SELECT id, name, number, paymentSystem FROM Cards) AS card ON cash.bankCardId = card.id
        WHERE cash.id IS NOT NULL AND (
            s.name LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%'
        ) 
        ORDER BY s.name ASC
        """
    )
    suspend fun searchShopsWithCashback(query: String): List<CategoryShopDB>

    @Query("SELECT * FROM Shops WHERE id = :id")
    suspend fun getShopById(id: Long): ShopDB?

    @Transaction
    @Query(
        """
        SELECT s.id, s.name, 
               c.id AS category_id, c.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount,
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Shops AS s
        LEFT JOIN Categories AS c ON s.categoryId = c.id
        LEFT JOIN Cashbacks AS cash ON 
            s.id = cash.shopId AND 
            cash.amount = (SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id)
        LEFT JOIN (SELECT id, name, number, paymentSystem FROM Cards) AS card ON cash.bankCardId = card.id
        WHERE s.id = :id
        """
    )
    suspend fun getCategoryShopById(id: Long): CategoryShopDB?
}