package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicInfoCategory
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun addCategory(category: BasicInfoCategory): Result<Unit>

    suspend fun updateCategory(category: BasicInfoCategory): Result<Unit>

    fun fetchCategories(): Flow<List<BasicInfoCategory>>

    suspend fun getCategoryById(id: Long): Result<Category>

    suspend fun deleteCategory(id: Long): Result<Unit>
}