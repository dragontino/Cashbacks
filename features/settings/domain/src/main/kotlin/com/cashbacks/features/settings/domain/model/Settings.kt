package com.cashbacks.features.settings.domain.model

data class Settings(
    val colorDesign: ColorDesign = ColorDesign.System,
    val dynamicColor: Boolean = false,
    val autoDeleteExpiredCashbacks: Boolean = true
)