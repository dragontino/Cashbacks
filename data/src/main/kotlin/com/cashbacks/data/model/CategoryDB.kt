package com.cashbacks.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    fun mapToCategory() = Category(id, name, maxCashback = null)
}


data class CategoryWithCashbackDB(
    val id: Long,
    val name: String,
    @Embedded(prefix = "cashback_")
    val maxCashbackDB: BasicCashbackDB?
) {
    fun mapToCategory() = Category(
        id = id,
        name = name,
        maxCashback = maxCashbackDB?.mapToCashback()
    )
}
