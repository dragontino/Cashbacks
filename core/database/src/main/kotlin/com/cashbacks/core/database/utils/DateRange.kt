package com.cashbacks.core.database.utils

import com.cashbacks.common.utils.DateUtils
import com.cashbacks.common.utils.today
import com.cashbacks.core.database.entity.BasicCashbackEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate


fun BasicCashbackEntity.getDateRange(): ClosedRange<LocalDate> {
    val startDate = startDate ?: Clock.System.today()
    val endDate = expirationDate ?: DateUtils.MaxDate
    return startDate..endDate
}