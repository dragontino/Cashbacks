package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchAllShopsUseCase {
    operator fun invoke(): Flow<List<CategoryShop>>
}


internal class FetchAllShopsUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchAllShopsUseCase {
    private companion object {
        const val TAG = "FetchAllShopsUseCase"
    }

    override fun invoke(): Flow<List<CategoryShop>> {
        return repository.fetchAllShops()
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}