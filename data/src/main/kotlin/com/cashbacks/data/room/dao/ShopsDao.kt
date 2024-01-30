package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.model.ShopWithMaxCashbackDB
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addShop(shop: ShopDB): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateShop(shop: ShopDB): Int

    @Delete
    suspend fun deleteShop(shop: ShopDB): Int

    @Query(
        """
        SELECT s.id, s.name, 
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Shops AS s
        LEFT JOIN (
            SELECT id, shopId, amount, expirationDate, comment, bankCardId 
            FROM Cashbacks
            ORDER BY amount DESC
            LIMIT 1
        ) AS cash ON s.id = cash.shopId
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE s.categoryId = :categoryId
        ORDER BY s.name ASC
        """
    )
    fun fetchAllShops(categoryId: Long): Flow<List<ShopWithMaxCashbackDB>>

    @Query(
        """
        SELECT s.id, s.name, 
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Shops AS s
        LEFT JOIN (
            SELECT id, shopId, amount, expirationDate, comment, bankCardId 
            FROM Cashbacks
            ORDER BY amount DESC
            LIMIT 1
        ) AS cash ON s.id = cash.shopId
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE s.categoryId = :categoryId AND cashback_id IS NOT NULL
        ORDER BY s.name ASC
        """
    )
    fun fetchShopsWithCashback(categoryId: Long): Flow<List<ShopWithMaxCashbackDB>>

    @Transaction
    @Query("SELECT * FROM Shops WHERE id = :id")
    suspend fun getShopById(id: Long): ShopDB?
}