package com.cashbacks.data.repository

import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.room.dao.CategoriesDao
import com.cashbacks.domain.model.BasicInfoCategory
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoriesDao) : CategoryRepository {
    override suspend fun addCategory(category: BasicInfoCategory): Result<Unit> {
        if (!checkCategoryNameForUniqueness(category.name)) {
            return Result.failure(EntryAlreadyExistsException)
        }

        val newId = dao.addCategory(CategoryDB(category))
        return when {
            newId < 0 -> Result.failure(InsertionException("Не удалось добавить категорию в базу данных"))
            else -> Result.success(Unit)
        }
    }

    private suspend fun checkCategoryNameForUniqueness(categoryName: String): Boolean {
        return dao.getCategoriesByName(categoryName).isEmpty()
    }


    override suspend fun updateCategory(category: BasicInfoCategory): Result<Unit> = try {
        if (!checkCategoryNameForUniqueness(category.name)) {
            throw EntryAlreadyExistsException
        }
        dao.updateCategory(CategoryDB(category))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun fetchCategories(): Flow<List<BasicInfoCategory>> {
        return dao.fetchCategories().map { list -> list.map { it.mapToCategory() } }
    }

    override suspend fun getCategoryById(id: Long): Result<Category> {
        return when (val category = dao.getCategory(id)) {
            null -> Result.failure(Exception())
            else -> Result.success(category)
        }
    }

    override suspend fun deleteCategory(id: Long): Result<Unit> {
        val success = dao.deleteCategoryById(id) > 0
        return if (success) Result.success(Unit) else Result.failure(Exception())
    }
}