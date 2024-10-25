package com.cashbacks.domain.model

data class Settings(
    val colorDesign: ColorDesign = ColorDesign.System,
    val dynamicColor: Boolean = false,
    val autoDeleteExpiredCashbacks: Boolean = true
)
