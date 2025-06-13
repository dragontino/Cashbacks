package com.cashbacks.features.cashback.domain.utils

import com.cashbacks.common.utils.DateUtils
import com.cashbacks.common.utils.today
import com.cashbacks.features.cashback.domain.model.Cashback
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

fun Cashback.getDateRange(): ClosedRange<LocalDate> {
    val startDate = startDate ?: Clock.System.today()
    val endDate = expirationDate ?: DateUtils.MaxDate
    return startDate..endDate
}


inline fun <T : Comparable<T>, R : Comparable<R>> ClosedRange<T>.map(
    block: (T) -> R
): ClosedRange<R> {
    return block(start)..block(endInclusive)
}