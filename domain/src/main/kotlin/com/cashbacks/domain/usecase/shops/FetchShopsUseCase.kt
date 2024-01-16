package com.cashbacks.domain.usecase.shops

import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

class FetchShopsUseCase(private val repository: ShopRepository) {

    fun fetchShopsFromCategory(categoryId: Long): Flow<List<Shop>> {
        return repository.fetchShopsFromCategory(categoryId)
    }
}