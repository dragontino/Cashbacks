package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun addShopToCategory(categoryId: Long, shop: Shop): Result<Unit>

    suspend fun updateShopInCategory(categoryId: Long, shop: Shop): Result<Unit>

    suspend fun deleteShopFromCategory(categoryId: Long, shop: Shop): Result<Unit>

    suspend fun getShopById(id: Long): Result<Shop>

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>>
}