package com.cashbacks.features.cashback.domain.model

import com.cashbacks.common.utils.today
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed class Cashback {
    abstract val id: Long
    abstract val bankCard: BasicBankCard
    abstract val amount: String
    abstract val measureUnit: MeasureUnit
    abstract val startDate: LocalDate?
    abstract val expirationDate: LocalDate?
    abstract val comment: String
}


@Serializable
data class BasicCashback(
    override val id: Long,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val measureUnit: MeasureUnit = MeasureUnit.Percent,
    override val startDate: LocalDate? = Clock.System.today(),
    override val expirationDate: LocalDate? = null,
    override val comment: String = ""
) : Cashback()


@Serializable
data class FullCashback(
    override val id: Long,
    val owner: CashbackOwner,
    override val bankCard: BasicBankCard,
    override val amount: String,
    override val measureUnit: MeasureUnit = MeasureUnit.Percent,
    override val startDate: LocalDate? = Clock.System.today(),
    override val expirationDate: LocalDate? = null,
    override val comment: String = ""
) : Cashback() {
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