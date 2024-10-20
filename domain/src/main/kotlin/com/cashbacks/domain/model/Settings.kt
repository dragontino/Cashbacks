package com.cashbacks.domain.model

import kotlinx.datetime.TimeZone

data class Settings(
    val colorDesign: ColorDesign = ColorDesign.System,
    val dynamicColor: Boolean = false,
    val usedTimeZone: TimeZone = TimeZone.currentSystemDefault()
)
