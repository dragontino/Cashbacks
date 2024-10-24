package com.cashbacks.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.cashbacks.domain.util.LocalDateParceler
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

sealed interface Cashback : Parcelable {
    val id: Long
    val bankCard: BasicBankCard
    val amount: String
    val calculationUnit: CalculationUnit
    val expirationDate: LocalDate?
    val comment: String
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