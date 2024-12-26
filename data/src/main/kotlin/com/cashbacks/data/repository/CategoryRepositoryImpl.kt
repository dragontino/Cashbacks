package com.cashbacks.data.repository

import com.cashbacks.data.model.CategoryDB
import com.cashbacks.data.room.dao.CategoriesDao
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.EntityException
import com.cashbacks.domain.model.EntityNotFoundException
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.FullCategory
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoriesDao) : CategoryRepository {
    override suspend fun addCategory(category: Category): Result<Long> {
        if (!checkCategoryNameForUniqueness(category.name)) {
            return Result.failure(EntryAlreadyExistsException(EntityException.Type.Category))
        }

        val newId = dao.addCategory(CategoryDB(category))
        return when {
            newId == null || newId < 0L -> Result.failure(
                InsertionException(EntityException.Type.Category, category.name)
            )
            else -> Result.success(newId)
        }
    }

    private suspend fun checkCategoryNameForUniqueness(categoryName: String): Boolean {
        return dao.getNumberOfCategoriesWithSameName(categoryName) == 0
    }


    override suspend fun updateCategory(category: Category): Result<Unit> = try {
        if (!checkCategoryNameForUniqueness(category.name)) {
            throw EntryAlreadyExistsException(EntityException.Type.Category)
        }

        dao.updateCategory(CategoryDB(category))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


    override fun fetchAllCategories(): Flow<List<BasicCategory>> {
        return dao.fetchAllCategories().map { list -> list.map { it.mapToDomainCategory() } }
    }


    override fun fetchCategoriesWithCashback(): Flow<List<BasicCategory>> {
        return dao.fetchCategoriesWithCashback().map { list ->
            list.map { it.mapToDomainCategory() }
        }
    }


    override suspend fun searchCategories(query: String, cashbacksRequired: Boolean): List<BasicCategory> {
        return when {
            cashbacksRequired -> dao.searchCategoriesWithCashback(query)
            else -> dao.searchAllCategories(query)
        }.map { it.mapToDomainCategory() }
    }


    override fun fetchCategoryById(id: Long): Flow<FullCategory> {
        return dao.fetchCategoryById(id)
    }


    override suspend fun getCategoryById(id: Long): Result<Category> {
        return when (val category = dao.getBasicCategoryById(id)) {
            null -> Result.failure(EntityNotFoundException(EntityException.Type.Category, id.toString()))
            else -> Result.success(category.mapToDomainCategory())
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        val success = dao.deleteCategory(CategoryDB(category)) > 0
        return when {
            success -> Result.success(Unit)
            else -> Result.failure(Exception())
        }
    }
}