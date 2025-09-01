package com.cashbacks.features.cashback.presentation.impl.mvi.model

import androidx.compose.runtime.Immutable
import com.cashbacks.common.utils.now
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.domain.utils.CashbackUtils.roundedAmount
import com.cashbacks.features.cashback.presentation.api.CashbackOwnerType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@Immutable
internal data class EditableCashback(
    val id: Long?,
    val ownerType: CashbackOwnerType,
    val owner: CashbackOwner?,
    val bankCard: BasicBankCard?,
    val amount: String,
    val measureUnit: MeasureUnit,
    val startDate: LocalDate?,
    val expirationDate: LocalDate?,
    val comment: String
) : java.io.Serializable {
    constructor(ownerType: CashbackOwnerType = CashbackOwnerType.Category) : this(
        id = null,
        ownerType = ownerType,
        owner = null,
        bankCard = null,
        amount = "",
        measureUnit = MeasureUnit.Percent,
        startDate = LocalDate.now(),
        expirationDate = null,
        comment = ""
    )

    fun copyFromCashback(cashback: FullCashback): EditableCashback? {
        val ownerType = when (cashback.owner) {
            is CashbackOwner.Category -> CashbackOwnerType.Category
            is CashbackOwner.Shop -> CashbackOwnerType.Shop
        }
        return when (this.ownerType) {
            ownerType -> copy(
                id = cashback.id,
                owner = cashback.owner,
                bankCard = cashback.bankCard,
                amount = cashback.roundedAmount,
                measureUnit = cashback.measureUnit,
                startDate = cashback.startDate,
                expirationDate = cashback.expirationDate,
                comment = cashback.comment,
            )

            else -> null
        }
    }


    fun updateStartDate(newDate: LocalDate?): EditableCashback {
        val newDate = newDate ?: LocalDate.now()
        return copy(
            startDate = newDate,
            expirationDate = when {
                expirationDate == null || expirationDate >= newDate -> expirationDate
                else -> null
            }
        )
    }

    fun updateAmount(newAmount: String): EditableCashback {
        val doubleAmount = newAmount.toDoubleOrNull()
        val isAmountCorrect = when {
            doubleAmount == null -> true
            measureUnit is MeasureUnit.Currency -> doubleAmount >= 0
            doubleAmount in 0.0..100.0 -> true
            else -> false
        }
        return when {
            isAmountCorrect -> copy(amount = newAmount)
            else -> this
        }
    }

    fun updateMeasureUnit(newUnit: MeasureUnit): EditableCashback {
        val amount = when {
            newUnit is MeasureUnit.Currency -> amount
            amount.toDoubleOrNull()?.let { it !in 0.0..100.0 } != true -> amount
            else -> ""
        }

        return copy(
            measureUnit = newUnit,
            amount = amount
        )
    }


    fun mapToCashback(): FullCashback? {
        return FullCashback(
            id = this.id ?: 0L,
            owner = owner ?: return null,
            bankCard = bankCard ?: return null,
            amount = this.amount,
            measureUnit = measureUnit,
            startDate = startDate,
            expirationDate = expirationDate,
            comment = this.comment
        )
    }
}