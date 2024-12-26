package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.data.model.BasicCategoryDB
import com.cashbacks.data.model.CategoryDB
import com.cashbacks.domain.model.FullCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface CategoriesDao : BaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategory(category: CategoryDB): Long?

    @Update(entity = CategoryDB::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCategory(category: CategoryDB)

    @Delete
    suspend fun deleteCategory(category: CategoryDB): Int


    @Query("SELECT COUNT(name) FROM Categories WHERE name = :name")
    suspend fun getNumberOfCategoriesWithSameName(name: String): Int

    @Query("SELECT * FROM Categories WHERE id = :id")
    fun fetchBasicCategoryById(id: Long): Flow<CategoryDB>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, 
               cash.amount AS cashback_amount, cash.measureUnit AS cashback_measureUnit, 
               cash.startDate AS cashback_startDate, cash.expirationDate AS cashback_expirationDate, 
               cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Categories AS cat
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON cat.id = cash.categoryId AND cash.amount = (
            SELECT MAX(amount) 
            FROM Cashbacks 
            WHERE categoryId = cat.id
                AND (startDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') >= startDate) 
                AND (expirationDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') <= expirationDate)
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        ORDER BY cat.name ASC
        """,
    )
    fun fetchAllCategories(): Flow<List<BasicCategoryDB>>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, 
               cash.amount AS cashback_amount, cash.measureUnit AS cashback_measureUnit, 
               cash.startDate AS cashback_startDate, cash.expirationDate AS cashback_expirationDate, 
               cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Categories AS cat
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON cat.id = cash.categoryId AND cash.amount = (
            SELECT MAX(amount)
            FROM Cashbacks 
            WHERE categoryId = cat.id
                AND (startDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') >= startDate) 
                AND (expirationDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') <= expirationDate)
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE cashback_id IS NOT NULL
        ORDER BY cat.name ASC
        """
    )
    fun fetchCategoriesWithCashback(): Flow<List<BasicCategoryDB>>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, 
               cash.amount AS cashback_amount, cash.measureUnit AS cashback_measureUnit, 
               cash.startDate AS cashback_startDate, cash.expirationDate AS cashback_expirationDate, 
               cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Categories AS cat
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON cat.id = cash.categoryId AND cash.amount = (
            SELECT MAX(amount) 
            FROM Cashbacks 
            WHERE categoryId = cat.id
                AND (startDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') >= startDate) 
                AND (expirationDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') <= expirationDate)
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE cat.name LIKE '%' || :query || '%'
        ORDER BY cat.name ASC
        """
    )
    suspend fun searchAllCategories(query: String): List<BasicCategoryDB>


    @Query(
        """
        SELECT cat.id, cat.name, 
               cash.id AS cashback_id, 
               cash.amount AS cashback_amount, cash.measureUnit AS cashback_measureUnit, 
               cash.startDate AS cashback_startDate, cash.expirationDate AS cashback_expirationDate, 
               cash.comment AS cashback_comment,
               card.id AS cashback_card_id, card.name AS cashback_card_name,
               card.number AS cashback_card_number, card.paymentSystem AS cashback_card_paymentSystem,
               card.maxCashbacksNumber AS cashback_card_maxCashbacksNumber
        FROM Categories AS cat
        LEFT JOIN (SELECT * FROM Cashbacks) AS cash 
        ON cat.id = cash.categoryId AND cash.amount = (
            SELECT MAX(amount) 
            FROM Cashbacks 
            WHERE categoryId = cat.id
                AND (startDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') >= startDate) 
                AND (expirationDate IS NULL OR strftime('%d/%m/%Y', 'now', 'localtime') <= expirationDate)
        )
        LEFT JOIN Cards AS card ON cash.bankCardId = card.id
        WHERE cashback_id IS NOT NULL AND cat.name LIKE '%' || :query || '%'
        ORDER BY cat.name ASC
        """
    )
    suspend fun searchCategoriesWithCashback(query: String): List<BasicCategoryDB>

    fun fetchCategoryById(id: Long): Flow<FullCategory> {
        val basicCategoryFlow = fetchBasicCategoryById(id)
        val shopsFlow = fetchShopsWithCashbackFromCategory(id)
        val cashbacksFlow = fetchCashbacksFromCategory(id)
        return combine(
            flow = basicCategoryFlow,
            flow2 = shopsFlow,
            flow3 = cashbacksFlow
        ) { basicCategory, shops, cashbacks ->
            FullCategory(
                id = basicCategory.id,
                name = basicCategory.name,
                shops = shops.map { it.mapToDomainShop() },
                cashbacks = cashbacks.map { it.mapToDomainCashback() }
            )
        }
    }
}