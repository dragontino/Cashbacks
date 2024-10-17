package com.cashbacks.data.repository

import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.room.dao.ShopsDao
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.CategoryShop
import com.cashbacks.domain.model.DeletionException
import com.cashbacks.domain.model.EntityNotFoundException
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

    override suspend fun addShop(shop: CategoryShop): Result<Long> {
        return addShop(ShopDB(shop))
    }


    override suspend fun addShop(categoryId: Long, shop: Shop): Result<Long> {
        return addShop(ShopDB(categoryId, shop))
    }


    private suspend fun addShop(shop: ShopDB): Result<Long> {
        if (!checkShopNameForUniqueness(shop.name)) {
            return Result.failure(EntryAlreadyExistsException(BasicShop::class))
        }

        return dao.addShop(shop).let { id ->
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


    override suspend fun updateShop(shop: CategoryShop): Result<Unit> {
        val shopDB = ShopDB(shop)
        return dao.updateShop(shopDB).let { updatedCount ->
            when {
                updatedCount < 0 -> Result.failure(
                    UpdateException(type = BasicShop::class, name = shop.name)
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
            .getCategoryShopById(id)
            ?.let { Result.success(it.mapToCategoryShop()) }
            ?: Result.failure(EntityNotFoundException(type = BasicShop::class, id = id.toString()))
    }


    override fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<BasicShop>> {
        return dao.fetchAllShopsFromCategory(categoryId).mapLatest { list ->
            list.map { it.mapToDomainShop() }
        }
    }

    override fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<BasicShop>> {
        return dao.fetchShopsWithCashbackFromCategory(categoryId).mapLatest { list ->
            list.map { it.mapToDomainShop() }
        }
    }

    override fun fetchAllShops(): Flow<List<BasicCategoryShop>> {
        return dao.fetchAllShops().mapLatest { list ->
            list.map { it.mapToCategoryShop() }
        }
    }

    override fun fetchShopsWithCashback(): Flow<List<BasicCategoryShop>> {
        return dao.fetchShopsWithCashback().mapLatest { list ->
            list.map { it.mapToCategoryShop() }
        }
    }

    override suspend fun searchShops(
        query: String,
        cashbacksRequired: Boolean
    ): List<BasicCategoryShop> {
        return when {
            cashbacksRequired -> dao.searchShopsWithCashback(query)
            else -> dao.searchAllShops(query)
        }.map { it.mapToCategoryShop() }
    }
}