package com.cashbacks.domain.repository

import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun addShopToCategory(categoryId: Long, shop: Shop): Result<Long>

    suspend fun updateShop(categoryId: Long, shop: Shop): Result<Unit>

    suspend fun deleteShop(shop: Shop): Result<Unit>

    suspend fun getShopById(id: Long): Result<CategoryShop>

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchAllShops(): Flow<List<CategoryShop>>

    fun fetchShopsWithCashbacks(): Flow<List<CategoryShop>>

    suspend fun searchShops(query: String, cashbacksRequired: Boolean): List<CategoryShop>
}