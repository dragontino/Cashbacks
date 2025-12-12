package com.cashbacks.core.database.source

import com.cashbacks.core.database.room.dao.CategoriesDao
import com.cashbacks.core.database.room.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    suspend fun addCategory(category: CategoryEntity): Long?
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity): Int

    suspend fun getNumberOfCategoriesWithSameName(name: String): Int
    suspend fun getCategoryById(id: Long): CategoryEntity?
    fun fetchAllCategories(): Flow<List<CategoryEntity>>
    fun fetchCategoriesWithCashback(): Flow<List<CategoryEntity>>
    suspend fun searchAllCategories(query: String): List<CategoryEntity>
    suspend fun searchCategoriesWithCashback(query: String): List<CategoryEntity>
    fun fetchCategoryById(id: Long): Flow<CategoryEntity>
}


internal class CategoryLocalDataSourceImpl(private val dao: CategoriesDao) :
    CategoryLocalDataSource {
    override suspend fun addCategory(category: CategoryEntity): Long? {
        return dao.addCategory(category)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity): Int {
        return dao.deleteCategory(category)
    }

    override suspend fun getNumberOfCategoriesWithSameName(name: String): Int {
        return dao.getNumberOfCategoriesWithSameName(name)
    }

    override suspend fun getCategoryById(id: Long): CategoryEntity? {
        return dao.getBasicCategoryById(id)
    }

    override fun fetchAllCategories(): Flow<List<CategoryEntity>> {
        return dao.fetchAllCategories()
    }

    override fun fetchCategoriesWithCashback(): Flow<List<CategoryEntity>> {
        return dao.fetchCategoriesWithCashback()
    }

    override suspend fun searchAllCategories(query: String): List<CategoryEntity> {
        return dao.searchAllCategories(query)
    }

    override suspend fun searchCategoriesWithCashback(query: String): List<CategoryEntity> {
        return dao.searchCategoriesWithCashback(query)
    }

    override fun fetchCategoryById(id: Long): Flow<CategoryEntity> {
        return dao.fetchCategoryById(id)
    }

}