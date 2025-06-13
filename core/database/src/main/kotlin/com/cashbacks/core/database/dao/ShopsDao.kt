package com.cashbacks.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.core.database.entity.CategoryShopEntity
import com.cashbacks.core.database.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addShop(shop: ShopEntity): Long

    @Query(
        """
            SELECT COUNT(name) 
            FROM Shops 
            WHERE categoryId = :categoryId AND name = :shopName
            """
    )
    suspend fun getShopsNumberWithSameNameFromCategory(categoryId: Long, shopName: String): Int

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateShop(shop: ShopEntity)

    @Delete
    suspend fun deleteShop(shop: ShopEntity): Int

    @Query("DELETE FROM Shops WHERE id = :id")
    suspend fun deleteShopById(id: Long): Int


    @Query(
        """
            SELECT s.id, s.name, cat.id AS category_id, cat.name AS category_name
            FROM Shops s
            LEFT JOIN Categories cat ON s.categoryId = cat.id
            ORDER BY s.name ASC
        """
    )
    fun fetchAllCategoryShops(): Flow<List<CategoryShopEntity>>

    @Query(
        """
            SELECT s.id, s.name, cat.id AS category_id, cat.name AS category_name
            FROM Shops AS s
            LEFT JOIN Categories cat ON s.categoryId = cat.id
            WHERE (SELECT COUNT(id) FROM Cashbacks WHERE shopId = s.id) > 0
            ORDER BY s.name ASC
        """
    )
    fun fetchCategoryShopsWithCashback(): Flow<List<CategoryShopEntity>>


    @Query(
        """
            SELECT s.id, s.name, c.id AS category_id, c.name AS category_name
            FROM Shops s
            LEFT JOIN Categories c ON s.categoryId = c.id
            WHERE s.name LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%'
            ORDER BY s.name ASC
        """
    )
    suspend fun searchAllCategoryShops(query: String): List<CategoryShopEntity>

    @Transaction
    @Query(
        """
            SELECT s.id, s.name, cat.id AS category_id, cat.name AS category_name
            FROM Shops s
            LEFT JOIN Categories cat ON s.categoryId = cat.id
            WHERE (SELECT COUNT(id) FROM Cashbacks WHERE shopId = s.id) > 0 AND (
                s.name LIKE '%' || :query || '%' OR category_name LIKE '%' || :query || '%'
            ) 
            ORDER BY s.name ASC
        """
    )
    suspend fun searchCategoryShopsWithCashback(query: String): List<CategoryShopEntity>


    @Query(
        """
            SELECT s.id, s.name, c.id AS category_id, c.name AS category_name
            FROM Shops s 
            LEFT JOIN Categories c ON s.categoryId = c.id
            WHERE s.id = :id
        """
    )
    suspend fun getCategoryShopEntityById(id: Long): CategoryShopEntity?


    @Query(
        """
            SELECT * 
            FROM Shops 
            WHERE categoryId = :categoryId 
            ORDER BY Shops.name ASC
        """
    )
    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<ShopEntity>>

    @Query(
        """
            SELECT * 
            FROM Shops s
            WHERE (SELECT COUNT(id) FROM Cashbacks WHERE shopId = s.id) > 0 
                AND s.categoryId = :categoryId
            ORDER BY s.name ASC
        """
    )
    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<ShopEntity>>
}