package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchShopsFromCategoryUseCase {
    operator fun invoke(categoryId: Long, cashbacksRequired: Boolean = false): Flow<List<Shop>>
}


internal class FetchShopsFromCategoryUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchShopsFromCategoryUseCase {
    private companion object {
        const val TAG = "FetchShopsFromCategoryUseCase"
    }

    override fun invoke(categoryId: Long, cashbacksRequired: Boolean): Flow<List<Shop>> {
        val flow = when {
            cashbacksRequired -> repository.fetchShopsWithCashbackFromCategory(categoryId)
            else -> repository.fetchAllShopsFromCategory(categoryId)
        }
        return flow.flowOn(dispatcher).catch {
            Log.e(TAG, it.message, it)
            throw it
        }
    }
}