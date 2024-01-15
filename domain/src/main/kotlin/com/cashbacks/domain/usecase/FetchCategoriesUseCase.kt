package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.BasicInfoCategory
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class FetchCategoriesUseCase(private val repository: CategoryRepository) {
    fun fetchCategories(): Flow<List<BasicInfoCategory>> {
        return repository.fetchCategories()
    }
}