package com.cashbacks.domain.repository

import com.cashbacks.domain.model.BasicInfoShop
import com.cashbacks.domain.model.Shop

interface ShopRepository {
    suspend fun addShopsToCategory(categoryId: Long, shops: List<BasicInfoShop>): List<Result<Unit>>

    suspend fun updateShopsInCategory(categoryId: Long, shops: List<BasicInfoShop>): List<Result<Unit>>

    suspend fun deleteShopsFromCategory(categoryId: Long, shops: List<BasicInfoShop>): List<Result<Unit>>

    suspend fun getShopById(id: Long): Result<Shop>
}