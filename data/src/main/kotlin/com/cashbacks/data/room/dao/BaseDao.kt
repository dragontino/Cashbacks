package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.cashbacks.data.model.BasicCashbackDB
import com.cashbacks.data.model.BasicShopDB
import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.model.ShopDB
import kotlinx.coroutines.flow.Flow

@Dao
interface BaseDao {
    @Query("SELECT * FROM Categories WHERE id = :id")
    suspend fun getBasicCategoryById(id: Long): CategoryDB?


    @Query("SELECT * FROM Shops WHERE categoryId = :categoryId")
    suspend fun getAllShopsFromCategory(categoryId: Long): List<ShopDB>


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
            WHERE s.categoryId = :categoryId AND cash.id IS NOT NULL
            ORDER BY s.name ASC
        """
    )
    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<BasicShopDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem,
                   card.maxCashbacksNumber AS card_maxCashbacksNumber
            FROM Cashbacks AS cash 
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId = :categoryId
            ORDER BY cash.amount ASC
        """
    )
    fun fetchCashbacksFromCategory(categoryId: Long): Flow<List<BasicCashbackDB>>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem,
                   card.maxCashbacksNumber AS card_maxCashbacksNumber
            FROM Cashbacks AS cash
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
        """
    )
    suspend fun getAllCashbacks(): List<BasicCashbackDB>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem,
                   card.maxCashbacksNumber AS card_maxCashbacksNumber
            FROM Cashbacks AS cash 
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.categoryId = :categoryId
        """
    )
    suspend fun getAllCashbacksFromCategory(categoryId: Long): List<BasicCashbackDB>


    @Query(
        """
            SELECT cash.id, cash.amount, cash.expirationDate, cash.comment,
                   card.id AS card_id,
                   card.name AS card_name,
                   card.number AS card_number,
                   card.paymentSystem AS card_paymentSystem,
                   card.maxCashbacksNumber AS card_maxCashbacksNumber
            FROM Cashbacks AS cash 
            INNER JOIN (SELECT * FROM Cards) AS card ON card.id = cash.bankCardId
            WHERE cash.shopId = :shopId
        """
    )
    suspend fun getAllCashbacksFromShop(shopId: Long): List<BasicCashbackDB>
}