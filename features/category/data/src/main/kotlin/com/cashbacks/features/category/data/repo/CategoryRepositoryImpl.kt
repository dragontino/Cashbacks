package com.cashbacks.features.category.data.repo

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.core.database.dao.CategoriesDao
import com.cashbacks.core.database.utils.mapList
import com.cashbacks.core.database.utils.mapToDomainCategory
import com.cashbacks.core.database.utils.mapToEntity
import com.cashbacks.features.category.data.resources.CategoryAlreadyExistsException
import com.cashbacks.features.category.data.resources.CategoryDeletionException
import com.cashbacks.features.category.data.resources.CategoryNotFoundException
import com.cashbacks.features.category.data.resources.InsertionException
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.domain.repo.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoriesDao,
    private val context: Context
) : CategoryRepository {
    override suspend fun addCategory(category: Category): Result<Long> {
        if (!checkCategoryNameForUniqueness(category.name)) {
            return Result.failure(CategoryAlreadyExistsException.toException(context))
        }

        val newId = dao.addCategory(category.mapToEntity())
        return when {
            newId == null || newId < 0L -> Result.failure(
                InsertionException(category.name).toException(context)
            )
            else -> Result.success(newId)
        }
    }

    private suspend fun checkCategoryNameForUniqueness(categoryName: String): Boolean {
        return dao.getNumberOfCategoriesWithSameName(categoryName) == 0
    }


    override suspend fun updateCategory(category: Category): Result<Unit> = runCatching {
        if (!checkCategoryNameForUniqueness(category.name)) {
            throw CategoryAlreadyExistsException.toException(context)
        }

        dao.updateCategory(category.mapToEntity())
    }


    override fun fetchAllCategories(): Flow<List<Category>> {
        return dao.fetchAllCategories().mapList { it.mapToDomainCategory() }
    }


    override fun fetchCategoriesWithCashback(): Flow<List<Category>> {
        return dao.fetchCategoriesWithCashback().mapList { it.mapToDomainCategory() }
    }


    override suspend fun searchCategories(
        query: String,
        cashbacksRequired: Boolean
    ): Result<List<Category>> = runCatching {
        val resultEntities = when {
            cashbacksRequired -> dao.searchCategoriesWithCashback(query)
            else -> dao.searchAllCategories(query)
        }
        return@runCatching resultEntities.map { it.mapToDomainCategory() }
    }


    override fun fetchCategoryById(id: Long): Flow<Category> {
        return dao.fetchCategoryById(id).map { it.mapToDomainCategory() }
    }


    override suspend fun getCategoryById(id: Long): Result<Category> {
        return when (val category = dao.getBasicCategoryById(id)) {
            null -> Result.failure(CategoryNotFoundException(id).toException(context))
            else -> Result.success(category.mapToDomainCategory())
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        val success = dao.deleteCategory(category.mapToEntity()) > 0
        return when {
            success -> Result.success(Unit)
            else -> Result.failure(CategoryDeletionException(category.name).toException(context))
        }
    }
}