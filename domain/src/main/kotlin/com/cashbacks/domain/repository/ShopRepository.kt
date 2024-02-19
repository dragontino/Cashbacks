package com.cashbacks.domain.repository

import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun addShopToCategory(categoryId: Long, shop: Shop): Result<Unit>

    suspend fun updateShop(shop: Shop): Result<Unit>

    suspend fun deleteShop(shop: Shop): Result<Unit>

    suspend fun getShopById(id: Long): Result<Shop>

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>>

    fun fetchAllShopsWithCategories(): Flow<List<Pair<Category, Shop>>>

    fun fetchShopsWithCategoriesAndCashbacks(): Flow<List<Pair<Category, Shop>>>
}