package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Category

@Entity(tableName = "Categories")
data class CategoryDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
) {
    constructor(category: Category) : this(
        id = category.id,
        name = category.name
    )

    fun mapToDomainCategory() = BasicCategory(id, name, maxCashback = null)
}


data class BasicCategoryDB(
    @Embedded
    val categoryDB: CategoryDB,
    @Embedded(prefix = "cashback_")
    val maxCashbackDB: BasicCashbackDB?
) {
    fun mapToDomainCategory() = categoryDB.mapToDomainCategory().copy(
        maxCashback = maxCashbackDB?.mapToDomainCashback()
    )
}
