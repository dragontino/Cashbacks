package com.cashbacks.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings

@Entity(tableName = "Settings")
data class SettingsDB(
    @ColumnInfo(defaultValue = "1")
    @PrimaryKey
    val id: Long = 1L,
    val colorDesign: String,
    val dynamicColor: Boolean,
    @ColumnInfo(name = "auto_delete", defaultValue = "1")
    val autoDeleteExpiredCashbacks: Boolean
) {
    constructor(settings: Settings) : this(
        colorDesign = settings.colorDesign.name,
        dynamicColor = settings.dynamicColor,
        autoDeleteExpiredCashbacks = settings.autoDeleteExpiredCashbacks
    )

    fun mapToDomainSettings() = Settings(
        colorDesign = ColorDesign.valueOf(this.colorDesign),
        dynamicColor = this.dynamicColor,
        autoDeleteExpiredCashbacks = this.autoDeleteExpiredCashbacks
    )
}