package com.cashbacks.data.util

import com.cashbacks.data.model.BasicCashbackDB
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate


internal fun Cashback.getDateRange(): ClosedRange<LocalDate> {
    val startDate = startDate ?: Clock.System.today()
    val endDate = expirationDate ?: LocalDate(2100, 12, 31)
    return startDate..endDate
}

internal fun BasicCashbackDB.getDateRange(): ClosedRange<LocalDate> {
    val startDate = startDate ?: Clock.System.today()
    val endDate = expirationDate ?: LocalDate(2100, 12, 31)
    return startDate..endDate
}

internal inline fun <T, R> ClosedRange<T>.map(block: (T) -> R): ClosedRange<R>
where T : Comparable<T>, R : Comparable<R> {
    return block(start)..block(endInclusive)
}