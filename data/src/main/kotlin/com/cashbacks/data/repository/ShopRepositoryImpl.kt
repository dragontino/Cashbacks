package com.cashbacks.data.repository

import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.room.dao.ShopsDao
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.DeletionException
import com.cashbacks.domain.model.EntryAlreadyExistsException
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.UpdateException
import com.cashbacks.domain.repository.ShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ShopRepositoryImpl(private val dao: ShopsDao) : ShopRepository {
    override suspend fun addShopToCategory(categoryId: Long, shop: Shop): Result<Long> {
        if (!checkShopNameForUniqueness(shop.name)) {
            return Result.failure(EntryAlreadyExistsException(Shop::class))
        }

        val shopDB = ShopDB(id = shop.id, categoryId = categoryId, name = shop.name)
        return dao.addShop(shopDB).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException(type = Shop::class, entityName = shop.name)
                )
                else -> Result.success(id)
            }
        }
    }


    private suspend fun checkShopNameForUniqueness(shopName: String): Boolean {
        return dao.countShopsWithSameName(shopName) == 0
    }


    override suspend fun updateShop(categoryId: Long, shop: Shop): Result<Unit> {
        val shopDB = ShopDB(categoryId, shop)
        return dao.updateShop(shopDB).let { updatedCount ->
            when {
                updatedCount < 0 -> Result.failure(
                    UpdateException(type = Shop::class, name = shop.name)
                )
                else -> Result.success(Unit)
            }
        }
    }

    override suspend fun deleteShop(shop: Shop): Result<Unit> {
        return dao.deleteShopById(shop.id).let { deletedCount ->
            when {
                deletedCount <= 0 -> Result.failure(
                    DeletionException(type = Shop::class, name = shop.name)
                )
                else -> Result.success(Unit)
            }
        }
    }

    override suspend fun getShopById(id: Long): Result<CategoryShop> {
        return dao
            .getShopById(id)
            ?.let { Result.success(it.mapToCategoryShop()) }
            ?: Result.failure(Exception())
    }

    override fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchAllShopsFromCategory(categoryId).mapLatest { list ->
            list.map { it.mapToShop() }
        }
    }

    override fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchShopsWithCashbackFromCategory(categoryId).mapLatest { list ->
            list.map { it.mapToShop() }
        }
    }

    override fun fetchAllShops(): Flow<List<CategoryShop>> {
        return dao.fetchAllShops().mapLatest { list ->
            list.map { it.mapToCategoryShop() }
        }
    }

    override fun fetchShopsWithCashbacks(): Flow<List<CategoryShop>> {
        return dao.fetchShopsWithCashback().mapLatest { list ->
            list.map { it.mapToCategoryShop() }
        }
    }

    override suspend fun searchShops(
        query: String,
        cashbacksRequired: Boolean
    ): List<CategoryShop> {
        return when {
            cashbacksRequired -> dao.searchShopsWithCashback(query)
            else -> dao.searchAllShops(query)
        }.map { it.mapToCategoryShop() }
    }
}