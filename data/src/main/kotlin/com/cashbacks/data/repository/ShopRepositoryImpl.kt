package com.cashbacks.data.repository

import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.room.dao.ShopsDao
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ShopRepositoryImpl(private val dao: ShopsDao) : ShopRepository {
    override suspend fun addShopToCategory(categoryId: Long, shop: Shop): Result<Unit> {
        val shopDB = ShopDB(id = shop.id, categoryId = categoryId, name = shop.name)
        return dao.addShop(shopDB).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException("Не удалось добавить магазин ${shop.name} в базу данных")
                )
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun updateShopInCategory(
        categoryId: Long,
        shop: Shop
    ): Result<Unit> {
        val shopDB = ShopDB(id = shop.id, categoryId = categoryId, name = shop.name)
        return dao.updateShop(shopDB).let { updatedCount ->
            when {
                updatedCount < 0 -> Result.failure(
                    InsertionException("Не удалось обновить все магазины")
                )
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun deleteShopFromCategory(
        categoryId: Long,
        shop: Shop
    ): Result<Unit> {
        val shopDB = ShopDB(id = shop.id, categoryId = categoryId, name = shop.name)
        return dao.deleteShop(shopDB).let { deletedCount ->
            when {
                deletedCount < 0 -> Result.failure(Exception())
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun getShopById(id: Long): Result<Shop> {
        return dao
            .getShopById(id)
            ?.let { Result.success(it.mapToShop()) }
            ?: Result.failure(Exception())
    }

    override fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchAllShops(categoryId).mapLatest { list ->
            list.map { it.mapToShop() }
        }
    }

    override fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchShopsWithCashback(categoryId).mapLatest { list ->
            list.map { it.mapToShop() }
        }
    }
}