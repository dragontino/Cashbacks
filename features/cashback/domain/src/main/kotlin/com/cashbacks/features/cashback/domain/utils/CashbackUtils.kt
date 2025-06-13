package com.cashbacks.features.cashback.domain.utils

import com.cashbacks.common.utils.today
import com.cashbacks.features.cashback.domain.model.Cashback
import kotlinx.datetime.Clock

object CashbackUtils {
    val Cashback.roundedAmount: String get() = amount
        .toDoubleOrNull()
        ?.takeIf { it % 1 == 0.0 }
        ?.toInt()?.toString()
        ?: amount


    val Cashback.displayableAmount get() = "$roundedAmount${measureUnit.getDisplayableString()}"


    internal fun List<Cashback>.filterMaxCashbacks(): Set<Cashback> {
        val today = Clock.System.today()
        return filter { today in it.getDateRange() }
            .groupBy { it.measureUnit }.values
            .mapNotNull { it.maxByOrNull(Cashback::amount) }
            .toSet()
    }
}