package com.cashbacks.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.cashbacks.domain.util.LocalDateParceler
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

sealed interface Cashback : Parcelable {
    val id: Long
    val bankCard: BasicBankCard
    val amount: String
    val measureUnit: MeasureUnit
    val startDate: LocalDate?
    val expirationDate: LocalDate?
    val comment: String
}


@Parcelize
@Immutable
data class BasicCashback(
    override val id: Long,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val measureUnit: MeasureUnit = MeasureUnit.Percent,
    override val startDate: @WriteWith<LocalDateParceler> LocalDate? = Clock.System.today(),
    override val expirationDate: @WriteWith<LocalDateParceler> LocalDate? = null,
    override val comment: String = ""
) : Cashback


@Parcelize
@Immutable
data class FullCashback(
    override val id: Long,
    val owner: CashbackOwner,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val measureUnit: MeasureUnit = MeasureUnit.Percent,
    override val startDate: @WriteWith<LocalDateParceler> LocalDate? = Clock.System.today(),
    override val expirationDate: @WriteWith<LocalDateParceler> LocalDate? = null,
    override val comment: String = ""
) : Cashback {
    constructor(basicCashback: BasicCashback, owner: CashbackOwner) : this(
        id = basicCashback.id,
        owner = owner,
        bankCard = basicCashback.bankCard,
        amount = basicCashback.amount,
        measureUnit = basicCashback.measureUnit,
        startDate = basicCashback.startDate,
        expirationDate = basicCashback.expirationDate,
        comment = basicCashback.comment
    )
}