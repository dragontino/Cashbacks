package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface SearchShopsUseCase {
    suspend operator fun invoke(
        query: String,
        cashbacksRequired: Boolean
    ): Result<List<CategoryShop>>
}


internal class SearchShopsUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : SearchShopsUseCase {
    private companion object {
        const val TAG = "SearchShopsUseCase"
    }

    override suspend fun invoke(
        query: String,
        cashbacksRequired: Boolean
    ): Result<List<CategoryShop>> {
        return withContext(dispatcher) {
            when {
                query.isBlank() -> Result.success(emptyList())
                else -> repository.searchShops(query, cashbacksRequired).onFailure {
                    Log.e(TAG, it.message, it)
                }
            }
        }
    }
}