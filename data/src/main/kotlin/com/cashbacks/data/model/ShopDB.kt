package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.Shop

@Entity(
    tableName = "Shops",
    foreignKeys = [
        ForeignKey(
            entity = CategoryDB::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class ShopDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val categoryId: Long,
    val name: String = "",
) {
    fun mapToShop() = Shop(
        id = id,
        name = name,
        maxCashback = null
    )
}


data class ShopWithMaxCashbackDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "cashback_")
    val maxCashback: BasicCashbackDB?
) {
    fun mapToShop() = Shop(
        id = id,
        name = name,
        maxCashback = maxCashback?.mapToCashback()
    )
}


data class ShopWithCategoryNameAndCashbackDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "category_")
    val category: CategoryDB,
    @Embedded(prefix = "cashback_")
    val maxCashback: BasicCashbackDB?
) {
    fun mapToShopCategoryPair() =
        category.mapToCategory() to Shop(id, name, maxCashback?.mapToCashback())
}
