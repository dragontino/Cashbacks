package com.cashbacks.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Cashback : Parcelable {
    val id: Long
    val bankCard: BasicBankCard
    val amount: String
    val expirationDate: String?
    val comment: String

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
data class BasicCashback(
    override val id: Long,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val expirationDate: String?,
    override val comment: String
) : Cashback


@Parcelize
data class FullCashback(
    override val id: Long,
    val owner: CashbackOwner,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val expirationDate: String?,
    override val comment: String
) : Cashback