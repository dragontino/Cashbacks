package com.cashbacks.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Settings")
data class SettingsEntity(
    @ColumnInfo(defaultValue = "1")
    @PrimaryKey
    val id: Long = 1L,
    val colorDesign: String,
    val dynamicColor: Boolean,
    @ColumnInfo(name = "auto_delete", defaultValue = "1")
    val autoDeleteExpiredCashbacks: Boolean
)