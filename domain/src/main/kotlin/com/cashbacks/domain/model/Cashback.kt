package com.cashbacks.domain.model

import android.os.Parcelable
import com.cashbacks.domain.util.DateTimeFormats
import com.cashbacks.domain.util.parseToDate
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

sealed interface Cashback : Parcelable {
    val id: Long
    val bankCard: BasicBankCard
    val amount: String
    val calculationUnit: CalculationUnit
    val expirationDate: LocalDate?
    val comment: String

    companion object {
        val DateFormat = DateTimeFormats.defaultDateFormat()
    }
}


val Cashback.roundedAmount: String get() = amount
    .toDoubleOrNull()
    ?.takeIf { it % 1 == 0.0 }
    ?.toInt()?.toString()
    ?: amount


fun Cashback.calculateNumberOfDaysBeforeExpiration(timeZone: TimeZone = TimeZone.currentSystemDefault()): Int {
    val today = Clock.System.todayIn(timeZone)
    val expirationDate = expirationDate?.parseToDate(Cashback.DateFormat) ?: return Int.MAX_VALUE
    return today.until(expirationDate, DateTimeUnit.DAY).coerceAtLeast(0)
}


@Parcelize
@Immutable
data class BasicCashback(
    override val id: Long,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val calculationUnit: CalculationUnit,
    override val expirationDate: @WriteWith<LocalDateParceler> LocalDate?,
    override val comment: String
) : Cashback


@Parcelize
@Immutable
data class FullCashback(
    override val id: Long,
    val owner: CashbackOwner,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val calculationUnit: CalculationUnit,
    override val expirationDate: @WriteWith<LocalDateParceler> LocalDate?,
    override val comment: String
) : Cashback