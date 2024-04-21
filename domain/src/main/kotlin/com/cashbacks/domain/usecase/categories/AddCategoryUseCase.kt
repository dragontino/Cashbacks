package com.cashbacks.domain.usecase.categories

import com.cashbacks.domain.model.Category
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AddCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addCategory(category: Category): Result<Long> {
        return withContext(dispatcher) {
            repository.addCategory(category)
        }
    }
}