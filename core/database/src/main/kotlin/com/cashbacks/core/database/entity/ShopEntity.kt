package com.cashbacks.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Shops",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class ShopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val categoryId: Long,
    val name: String = "",
)


data class CategoryShopEntity(
    val id: Long,
    val name: String,
    @Embedded(prefix = "category_")
    val category: CategoryEntity
)
