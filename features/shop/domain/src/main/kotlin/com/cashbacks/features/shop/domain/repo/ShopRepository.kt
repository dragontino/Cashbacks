package com.cashbacks.features.shop.domain.repo

import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun addShop(shop: CategoryShop): Result<Long>

    suspend fun addShop(categoryId: Long, shop: Shop) : Result<Long>

    suspend fun updateShop(shop: CategoryShop): Result<Unit>

    suspend fun deleteShop(shop: Shop): Result<Unit>

    suspend fun getShopById(id: Long): Result<CategoryShop>

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchAllShops(): Flow<List<CategoryShop>>

    fun fetchShopsWithCashback(): Flow<List<CategoryShop>>

    suspend fun searchShops(query: String, cashbacksRequired: Boolean): Result<List<CategoryShop>>
}