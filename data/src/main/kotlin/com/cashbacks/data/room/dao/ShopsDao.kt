package com.cashbacks.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashbacks.data.model.ShopDB
import com.cashbacks.data.model.ShopWithCashbacks
import com.cashbacks.domain.model.Shop

@Dao
abstract class ShopsDao : CardsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addShops(shops: List<ShopDB>): List<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun updateShops(shops: List<ShopDB>): Int

    @Transaction
    open suspend fun getShop(id: Long): Shop? {
        val shopWithCashbacks = getShopById(id) ?: return null
        val cashbacks = shopWithCashbacks.cashbacks.map {
            val bankCardDB = getBasicInfoAboutBankCardById(it.bankCardId)
            it.mapToCashback(bankCardDB.mapToBankCard())
        }

        return Shop(
            id = shopWithCashbacks.id,
            name = shopWithCashbacks.name,
            cashbacks = cashbacks
        )
    }

    @Transaction
    @Query("SELECT id, name FROM Shops WHERE id = :id")
    protected abstract suspend fun getShopById(id: Long): ShopWithCashbacks?

    @Delete
    abstract suspend fun deleteShops(vararg shops: ShopDB): Int
}