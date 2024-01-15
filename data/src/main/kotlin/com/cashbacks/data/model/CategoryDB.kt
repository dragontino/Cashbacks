package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.BasicInfoCategory

@Entity(tableName = "Categories")
data class CategoryDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
) {
    constructor(category: BasicInfoCategory) : this(
        id = category.id,
        name = category.name
    )
}


data class BasicCategoryDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "cashback_")
    val maxCashbackDB: CashbackWithBankCardDB?
) {
    fun mapToCategory() = BasicCategory(
        id = id,
        name = name,
        maxCashback = maxCashbackDB?.mapToCashback()
    )
}


data class CategoryWithShopsAndCashbacks(
    @Embedded
    val categoryDB: CategoryDB,
    @Relation(parentColumn = "id", entityColumn = "categoryId", entity = ShopDB::class)
    val shops: List<ShopDB>,
    @Relation(parentColumn = "id", entityColumn = "categoryId", entity = CashbackDB::class)
    val cashbacks: List<CashbackDB>
)
