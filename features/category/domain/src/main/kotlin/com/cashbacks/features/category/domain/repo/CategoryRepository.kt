package com.cashbacks.features.category.domain.repo

import com.cashbacks.features.category.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun addCategory(category: Category): Result<Long>

    suspend fun updateCategory(category: Category): Result<Unit>

    fun fetchAllCategories(): Flow<List<Category>>

    fun fetchCategoriesWithCashback(): Flow<List<Category>>

    suspend fun searchCategories(query: String, cashbacksRequired: Boolean): Result<List<Category>>

    fun fetchCategoryById(id: Long): Flow<Category>

    suspend fun getCategoryById(id: Long): Result<Category>

    suspend fun deleteCategory(category: Category): Result<Unit>
}