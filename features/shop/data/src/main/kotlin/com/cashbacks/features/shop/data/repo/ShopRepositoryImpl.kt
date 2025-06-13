package com.cashbacks.features.shop.data.repo

import android.content.Context
import com.cashbacks.common.resources.toException
import com.cashbacks.core.database.dao.ShopsDao
import com.cashbacks.core.database.entity.ShopEntity
import com.cashbacks.core.database.utils.mapList
import com.cashbacks.core.database.utils.mapToDomainShop
import com.cashbacks.core.database.utils.mapToEntity
import com.cashbacks.features.shop.data.resources.InsertionException
import com.cashbacks.features.shop.data.resources.ShopAlreadyExistsException
import com.cashbacks.features.shop.data.resources.ShopDeletionException
import com.cashbacks.features.shop.data.resources.ShopNotFoundException
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.domain.repo.ShopRepository
import kotlinx.coroutines.flow.Flow

internal class ShopRepositoryImpl(
    private val dao: ShopsDao,
    private val context: Context
) : ShopRepository {

    override suspend fun addShop(shop: CategoryShop): Result<Long> {
        return addShop(shop.mapToEntity())
    }

    override suspend fun addShop(categoryId: Long, shop: Shop): Result<Long> {
        return addShop(shop.mapToEntity(categoryId))
    }

    private suspend fun addShop(entity: ShopEntity): Result<Long> {
        if (!checkShopNameForUniqueness(entity.categoryId, entity.name)) {
            return Result.failure(ShopAlreadyExistsException.toException(context))
        }

        return dao.addShop(entity).let { id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException(shopName = entity.name).toException(context)
                )
                else -> Result.success(id)
            }
        }
    }

    override suspend fun updateShop(shop: CategoryShop): Result<Unit> {
        val entity = shop.mapToEntity()
        if (!checkShopNameForUniqueness(entity.categoryId, entity.name)) {
            return Result.failure(ShopAlreadyExistsException.toException(context))
        }

        return dao.updateShop(entity).let { Result.success(it) }
    }


    private suspend fun checkShopNameForUniqueness(categoryId: Long, shopName: String): Boolean {
        return dao.getShopsNumberWithSameNameFromCategory(categoryId, shopName) == 0
    }


    override suspend fun deleteShop(shop: Shop): Result<Unit> {
        return dao.deleteShopById(shop.id).let { deletedCount ->
            when {
                deletedCount <= 0 -> Result.failure(
                    ShopDeletionException(name = shop.name).toException(context)
                )
                else -> Result.success(Unit)
            }
        }
    }

    override suspend fun getShopById(id: Long): Result<CategoryShop> {
        return dao.getCategoryShopEntityById(id)
            ?.let { Result.success(it.mapToDomainShop()) }
            ?: Result.failure(ShopNotFoundException(id).toException(context))
    }

    override fun fetchAllShopsFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchAllShopsFromCategory(categoryId).mapList { it.mapToDomainShop() }
    }

    override fun fetchShopsWithCashbackFromCategory(categoryId: Long): Flow<List<Shop>> {
        return dao.fetchShopsWithCashbackFromCategory(categoryId).mapList { it.mapToDomainShop() }
    }

    override fun fetchAllShops(): Flow<List<CategoryShop>> {
        return dao.fetchAllCategoryShops().mapList { it.mapToDomainShop() }
    }

    override fun fetchShopsWithCashback(): Flow<List<CategoryShop>> {
        return dao.fetchCategoryShopsWithCashback().mapList { it.mapToDomainShop() }
    }

    override suspend fun searchShops(
        query: String,
        cashbacksRequired: Boolean
    ): Result<List<CategoryShop>> = runCatching {
        val list = when {
            cashbacksRequired -> dao.searchCategoryShopsWithCashback(query)
            else -> dao.searchAllCategoryShops(query)
        }
        return@runCatching list.map { it.mapToDomainShop() }
    }

}