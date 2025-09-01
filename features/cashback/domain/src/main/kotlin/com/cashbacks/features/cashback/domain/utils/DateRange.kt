package com.cashbacks.features.cashback.domain.utils

import com.cashbacks.common.utils.DateUtils
import com.cashbacks.common.utils.now
import com.cashbacks.features.cashback.domain.model.Cashback
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

fun Cashback.getDateRange(): LocalDateRange {
    val startDate = startDate ?: LocalDate.now()
    val endDate = expirationDate ?: DateUtils.MaxDate
    return startDate..endDate
}


inline fun <T : Comparable<T>, R : Comparable<R>> ClosedRange<T>.map(
    block: (T) -> R
): ClosedRange<R> {
    return block(start)..block(endInclusive)
}