package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.model.ShopWithCategoryNameAndCashbackDB
import com.cashbacks.data.model.ShopWithMaxCashbackDB
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addShop(shop: ShopDB): Long

    @Query("SELECT COUNT(name) FROM Shops WHERE name = :name")
    suspend fun countShopsWithSameName(name: String): Int

    @Query("UPDATE Shops SET name = :shopName WHERE id = :shopId")
    suspend fun updateShopById(shopId: Long, shopName: String): Int

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
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Shops AS s
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON s.id = cash.shopId AND cash.amount = (
            SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE s.categoryId = :categoryId
        ORDER BY s.name ASC
        """
    )
    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<ShopWithMaxCashbackDB>>

    @Query(
        """
        SELECT s.id, s.name, 
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Shops AS s
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON s.id = cash.shopId AND cash.amount = (
            SELECT MAX(amount) FROM Cashbacks WHERE shopId = s.id
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE s.categoryId = :categoryId AND cash.id IS NOT NULL
        ORDER BY s.name ASC
        """
    )
    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<ShopWithMaxCashbackDB>>

    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
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
    fun fetchAllShops(): Flow<List<ShopWithCategoryNameAndCashbackDB>>

    @Query(
        """
        SELECT s.id, s.name,
               cat.id AS category_id, cat.name AS category_name,
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
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
    fun fetchShopsWithCashback(): Flow<List<ShopWithCategoryNameAndCashbackDB>>

    @Transaction
    @Query("SELECT * FROM Shops WHERE id = :id")
    suspend fun getShopById(id: Long): ShopDB?
}