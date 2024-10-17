package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun addCategory(category: Category): Result<Long>

    suspend fun updateCategory(category: Category): Result<Unit>

    fun fetchAllCategories(): Flow<List<BasicCategory>>

    fun fetchCategoriesWithCashback(): Flow<List<BasicCategory>>

    suspend fun searchCategories(query: String, cashbacksRequired: Boolean): List<BasicCategory>

    fun fetchCategoryById(id: Long): Flow<FullCategory>

    suspend fun getCategoryById(id: Long): Result<Category>

    suspend fun deleteCategory(category: Category): Result<Unit>
}