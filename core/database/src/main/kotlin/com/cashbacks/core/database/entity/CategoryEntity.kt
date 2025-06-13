package com.cashbacks.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
)