package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun addShop(shop: CategoryShop): Result<Long>

    suspend fun addShop(categoryId: Long, shop: Shop) : Result<Long>

    suspend fun updateShop(shop: CategoryShop): Result<Unit>

    suspend fun deleteShop(shop: Shop): Result<Unit>

    suspend fun getShopById(id: Long): Result<CategoryShop>

    fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<BasicShop>>

    fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<BasicShop>>

    fun fetchAllShops(): Flow<List<BasicCategoryShop>>

    fun fetchShopsWithCashback(): Flow<List<BasicCategoryShop>>

    suspend fun searchShops(query: String, cashbacksRequired: Boolean): List<BasicCategoryShop>
}