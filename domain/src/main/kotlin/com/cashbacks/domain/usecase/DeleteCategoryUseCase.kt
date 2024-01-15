package com.cashbacks.domain.usecase

import com.cashbacks.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DeleteCategoryUseCase(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun deleteCategory(id: Long): Result<Unit> {
        return withContext(dispatcher) {
            repository.deleteCategory(id)
        }
    }
}