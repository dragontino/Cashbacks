package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.model.CategoryWithCashbackDB
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategory(category: CategoryDB): Long

    @Update(entity = CategoryDB::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateCategory(category: CategoryDB)

    @Delete
    suspend fun deleteCategory(category: CategoryDB): Int


    @Query("SELECT name FROM Categories WHERE name = :name")
    suspend fun getCategoriesByName(name: String): List<String>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Categories AS cat
        LEFT JOIN (
            SELECT id, categoryId, amount, expirationDate, comment, bankCardId 
            FROM Cashbacks
            ORDER BY amount DESC
            LIMIT 1
        ) AS cash ON cat.id = cash.categoryId
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        ORDER BY cat.name ASC
        """,
    )
    fun fetchAllCategories(): Flow<List<CategoryWithCashbackDB>>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, cash.amount AS cashback_amount, 
               cash.expirationDate AS cashback_expirationDate, cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem
        FROM Categories AS cat
        LEFT JOIN (
            SELECT id, categoryId, amount, expirationDate, comment, bankCardId 
            FROM Cashbacks
            ORDER BY amount DESC
            LIMIT 1
        ) AS cash ON cat.id = cash.categoryId
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE cashback_id IS NOT NULL
        ORDER BY cat.name ASC
        """
    )
    fun fetchCategoriesWithCashback(): Flow<List<CategoryWithCashbackDB>>


    @Query("SELECT * FROM Categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryDB?
}