package com.cashbacks.domain.usecase.categories

import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class FetchCategoriesUseCase(private val repository: CategoryRepository) {
    fun fetchCategories(): Flow<List<Category>> {
        return repository.fetchCategories()
    }
}