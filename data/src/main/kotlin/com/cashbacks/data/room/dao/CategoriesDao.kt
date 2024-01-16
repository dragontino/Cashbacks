package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.BasicCategoryDB
import com.cashbacks.data.model.CashbackDB
import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.model.CategoryWithShopsAndCashbacks
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoriesDao : CardsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addCategory(category: CategoryDB): Long

    @Update(entity = CategoryDB::class, onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun updateCategory(category: CategoryDB)

    @Query("DELETE FROM Categories WHERE id = :id")
    abstract suspend fun deleteCategoryById(id: Long): Int


    @Query("SELECT name FROM Categories WHERE name = :name")
    abstract suspend fun getCategoriesByName(name: String): List<String>


    @Transaction
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
    abstract fun fetchCategories(): Flow<List<BasicCategoryDB>>


    @Transaction
    open suspend fun getCategory(id: Long): Category? {
        val categoryWithShopsAndCashbacks = getCategoryById(id) ?: return null
        val shops = categoryWithShopsAndCashbacks.shops.map { shopDB ->
            val maxCashbackDB = getMaxCashbackByShop(shopDB.id)
            val bankCard = when (maxCashbackDB) {
                null -> null
                else -> getBasicInfoAboutBankCardById(maxCashbackDB.bankCardId)
            }
            return@map BasicShop(
                id = shopDB.id,
                name = shopDB.name,
                maxCashback = bankCard?.let { maxCashbackDB?.mapToCashback(it) }
            )
        }
        val cashbacks = categoryWithShopsAndCashbacks.cashbacks.map {
            val bankCard = getBasicInfoAboutBankCardById(it.bankCardId)
            it.mapToCashback(bankCard)
        }

        return Category(
            id = categoryWithShopsAndCashbacks.categoryDB.id,
            name = categoryWithShopsAndCashbacks.categoryDB.name,
            shops = shops,
            cashbacks = cashbacks
        )
    }


    @Transaction
    @Query("SELECT id, name FROM Categories WHERE id = :id")
    protected abstract suspend fun getCategoryById(id: Long): CategoryWithShopsAndCashbacks?


    @Query(
        """
            SELECT c.*
            FROM Cashbacks AS c
            WHERE shopId = :shopId
            AND amount = (SELECT MAX(amount) FROM Cashbacks WHERE shopId = :shopId)
        """
    )
    protected abstract suspend fun getMaxCashbackByShop(shopId: Long): CashbackDB?

}