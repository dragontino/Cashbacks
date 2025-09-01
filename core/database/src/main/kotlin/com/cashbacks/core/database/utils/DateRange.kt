package com.cashbacks.core.database.utils

import com.cashbacks.common.utils.DateUtils
import com.cashbacks.common.utils.now
import com.cashbacks.core.database.entity.BasicCashbackEntity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange


fun BasicCashbackEntity.getDateRange(): LocalDateRange {
    val startDate = startDate ?: LocalDate.now()
    val endDate = expirationDate ?: DateUtils.MaxDate
    return startDate..endDate
}