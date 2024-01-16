package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun addCategory(category: Category): Result<Unit>

    suspend fun updateCategory(category: Category): Result<Unit>

    fun fetchCategories(): Flow<List<Category>>

    suspend fun getCategoryById(id: Long): Result<Category>

    suspend fun deleteCategory(category: Category): Result<Unit>
}