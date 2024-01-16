package com.cashbacks.data.repository

import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.room.dao.ShopsDao
import com.cashbacks.domain.model.BasicInfoShop
import com.cashbacks.domain.model.InsertionException
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.repository.ShopRepository

class ShopRepositoryImpl(private val dao: ShopsDao) : ShopRepository {
    override suspend fun addShopsToCategory(
        categoryId: Long,
        shops: List<BasicInfoShop>
    ): List<Result<Unit>> {
        val shopsDB = shops.map {
            ShopDB(id = it.id, categoryId = categoryId, name = it.name)
        }
        return dao.addShops(shopsDB).mapIndexed { index, id ->
            when {
                id < 0 -> Result.failure(
                    InsertionException("Не удалось добавить магазин ${shops[index].name} в базу данных")
                )
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun updateShopsInCategory(
        categoryId: Long,
        shops: List<BasicInfoShop>
    ): Result<Unit> {
        val shopsDB = shops.map {
            ShopDB(id = it.id, categoryId = categoryId, name = it.name)
        }
        return dao.updateShops(shopsDB).let { updatedCount ->
            when {
                updatedCount < shops.size -> Result.failure(
                    InsertionException("Не удалось обновить все магазины")
                )
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun deleteShopsFromCategory(
        categoryId: Long,
        shops: List<BasicInfoShop>
    ): Result<Unit> {
        val shopsDB = shops
            .map { ShopDB(id = it.id, categoryId = categoryId, name = it.name) }
            .toTypedArray()

        return dao.deleteShops(shops = shopsDB).let { deletedCount ->
            when {
                deletedCount < shops.size -> Result.failure(Exception())
                else -> Result.success(Unit)
            }
        }
    }


    override suspend fun getShopById(id: Long): Result<Shop> {
        return dao
            .getShop(id)
            ?.let { Result.success(it) }
            ?: Result.failure(Exception())
    }
}