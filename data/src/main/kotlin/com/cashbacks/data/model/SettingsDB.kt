package com.cashbacks.data.model

import androidx.room.Entity
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings

@Entity(tableName = "Settings", primaryKeys = ["colorDesign", "dynamicColor"])
data class SettingsDB(
    val colorDesign: String,
    val dynamicColor: Boolean
) {
    constructor(settings: Settings) : this(
        colorDesign = settings.colorDesign.name,
        dynamicColor = settings.dynamicColor
    )

    fun mapToSettings() = Settings(
        colorDesign = ColorDesign.valueOf(this.colorDesign),
        dynamicColor = this.dynamicColor
    )
}