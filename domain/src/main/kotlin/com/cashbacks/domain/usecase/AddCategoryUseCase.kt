package com.cashbacks.domain.usecase

import com.cashbacks.domain.model.BasicInfoCategory
import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AddCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun addCategory(category: BasicInfoCategory): Result<Unit> {
        return withContext(dispatcher) {
            repository.addCategory(category)
        }
    }
}