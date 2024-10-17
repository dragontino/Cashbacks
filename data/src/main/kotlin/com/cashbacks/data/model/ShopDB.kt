package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.CategoryShop
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
    constructor(categoryId: Long, shop: Shop) : this(
        id = shop.id,
        categoryId = categoryId,
        name = shop.name
    )

    constructor(shop: CategoryShop) : this(
        id = shop.id,
        categoryId = shop.parent.id,
        name = shop.name
    )

    fun mapToDomainShop() = BasicShop(
        id = id,
        name = name,
        maxCashback = null
    )
}


data class BasicShopDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "cashback_")
    val maxCashback: BasicCashbackDB?
) {
    fun mapToDomainShop() = BasicShop(
        id = id,
        name = name,
        maxCashback = maxCashback?.mapToDomainCashback()
    )
}

data class CategoryShopDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "category_")
    val category: CategoryDB,
    @Embedded(prefix = "cashback_")
    val maxCashback: BasicCashbackDB?
) {
    fun mapToCategoryShop() = BasicCategoryShop(
        id = id,
        parent = category.mapToDomainCategory(),
        name = name,
        maxCashback = maxCashback?.mapToDomainCashback()
    )
}
