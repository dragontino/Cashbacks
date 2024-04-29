package com.cashbacks.data.repository

import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.room.dao.CategoriesDao
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.EntityNotFoundException
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoriesDao) : CategoryRepository {
    override suspend fun addCategory(category: Category): Result<Long> {
        if (!checkCategoryNameForUniqueness(category.name)) {
            return Result.failure(EntryAlreadyExistsException(Category::class))
        }

        val newId = dao.addCategory(CategoryDB(category))
        return when {
            newId < 0 -> Result.failure(InsertionException(Category::class, category.name))
            else -> Result.success(newId)
        }
    }

    private suspend fun checkCategoryNameForUniqueness(categoryName: String): Boolean {
        return dao.countCategoriesWithSameName(categoryName) == 0
    }


    override suspend fun updateCategory(category: Category): Result<Unit> = try {
        if (!checkCategoryNameForUniqueness(category.name)) {
            throw EntryAlreadyExistsException(Category::class)
        }
        dao.updateCategory(CategoryDB(category))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


    override fun fetchAllCategories(): Flow<List<Category>> {
        return dao.fetchAllCategories().map { list -> list.map { it.mapToCategory() } }
    }


    override fun fetchCategoriesWithCashback(): Flow<List<Category>> {
        return dao.fetchCategoriesWithCashback().map { list -> list.map { it.mapToCategory() } }
    }


    override suspend fun searchCategories(query: String, cashbacksRequired: Boolean): List<Category> {
        return when {
            cashbacksRequired -> dao.searchCategoriesWithCashback(query)
            else -> dao.searchAllCategories(query)
        }.map { it.mapToCategory() }
    }


    override suspend fun getCategoryById(id: Long): Result<Category> {
        return when (val category = dao.getCategoryById(id)) {
            null -> Result.failure(EntityNotFoundException(Category::class, id.toString()))
            else -> Result.success(category.mapToCategory())
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        val success = dao.deleteCategory(CategoryDB(category)) > 0
        return if (success) Result.success(Unit) else Result.failure(Exception())
    }
}