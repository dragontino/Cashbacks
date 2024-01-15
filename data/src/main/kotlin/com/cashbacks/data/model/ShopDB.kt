package com.cashbacks.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

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
)


data class ShopWithCashbacks(
    val id: Long,
    val name: String,
    @Relation(parentColumn = "id", entityColumn = "shopId")
    val cashbacks: List<CashbackDB>
)
