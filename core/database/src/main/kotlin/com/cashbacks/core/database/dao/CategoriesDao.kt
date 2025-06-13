package com.cashbacks.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashbacks.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun addCategory(category: CategoryEntity): Long?

    @Update(entity = CategoryEntity::class, onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity): Int


    @Query("SELECT COUNT(name) FROM Categories WHERE name = :name")
    suspend fun getNumberOfCategoriesWithSameName(name: String): Int


    @Query("SELECT * FROM Categories WHERE id = :id")
    suspend fun getBasicCategoryById(id: Long): CategoryEntity?


    @Query("SELECT * FROM Categories ORDER BY Categories.name ASC")
    fun fetchAllCategories(): Flow<List<CategoryEntity>>


    @Query(
        """
            SELECT cat.id, cat.name
            FROM Categories cat
            WHERE (SELECT COUNT(id) FROM Cashbacks WHERE categoryId = cat.id) > 0
            ORDER BY cat.name ASC
        """
    )
    fun fetchCategoriesWithCashback(): Flow<List<CategoryEntity>>


    @Query(
        """
            SELECT * FROM Categories cat 
            WHERE cat.name LIKE '%' || :query || '%' 
            ORDER BY cat.name ASC
        """
    )
    suspend fun searchAllCategories(query: String): List<CategoryEntity>


    @Query(
        """
            SELECT * FROM Categories cat
            WHERE (SELECT COUNT(id) FROM Cashbacks WHERE categoryId = cat.id) > 0 
                AND cat.name LIKE '%' || :query || '%'
            ORDER BY cat.name ASC
        """
    )
    suspend fun searchCategoriesWithCashback(query: String): List<CategoryEntity>


    @Query("SELECT * FROM Categories WHERE id = :id")
    fun fetchCategoryById(id: Long): Flow<CategoryEntity>
}