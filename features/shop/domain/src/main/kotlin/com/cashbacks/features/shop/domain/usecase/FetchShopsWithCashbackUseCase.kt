package com.cashbacks.features.shop.domain.usecase

import android.util.Log
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

interface FetchShopsWithCashbackUseCase {
    operator fun invoke(): Flow<List<CategoryShop>>
}


internal class FetchShopsWithCashbackUseCaseImpl(
    private val repository: ShopRepository,
    private val dispatcher: CoroutineDispatcher
) : FetchShopsWithCashbackUseCase {
    private companion object {
        const val TAG = "FetchShopsWithCashbackUseCase"
    }

    override fun invoke(): Flow<List<CategoryShop>> {
        return repository.fetchShopsWithCashback()
            .flowOn(dispatcher)
            .catch {
                Log.e(TAG, it.message, it)
                throw it
            }
    }
}