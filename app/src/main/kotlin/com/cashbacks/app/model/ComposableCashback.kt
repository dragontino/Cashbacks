package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.app.ui.features.cashback.CashbackOwnerType
import com.cashbacks.app.util.CashbackUtils.roundedAmount
import com.cashbacks.domain.model.BankCardNotSelectedException
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.CashbackOwner
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryNotSelectedException
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.IncorrectCashbackAmountException
import com.cashbacks.domain.model.MeasureUnit
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopNotSelectedException
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

@Stable
internal class ComposableCashback private constructor(
    id: Long?,
    private val ownerType: CashbackOwnerType,
    owner: CashbackOwner?,
    bankCard: BasicBankCard?,
    amount: String,
    measureUnit: MeasureUnit,
    startDate: LocalDate?,
    expirationDate: LocalDate?,
    comment: String
) : Updatable, ErrorHolder<CashbackError> {

    constructor(ownerType: CashbackOwnerType) : this(
        id = null,
        ownerType = ownerType,
        owner = null,
        bankCard = null,
        amount = "",
        measureUnit = MeasureUnit.Percent,
        startDate = Clock.System.today(),
        expirationDate = null,
        comment = ""
    )

    var id by mutableStateOf(id)
    var amount by mutableStateOf(amount)
    var measureUnit by mutableStateOf(measureUnit)
    var owner by mutableStateOf(owner)
    var bankCard by mutableStateOf(bankCard)
    var startDate by mutableStateOf(startDate)
    var expirationDate by mutableStateOf(expirationDate)
    var comment by mutableStateOf(comment)

    private val errorMessages = mutableStateMapOf<CashbackError, String>()
    override val errors: Map<CashbackError, String> get() = errorMessages.toMap()

    override val updatedProperties = mutableStateMapOf<String, Pair<String, String>>()

    fun updateCashback(cashback: FullCashback) {
        val ownerType = when (cashback.owner) {
            is Category -> CashbackOwnerType.Category
            is Shop -> CashbackOwnerType.Shop
        }
        if (this.ownerType == ownerType) {
            id = cashback.id
            owner = cashback.owner
            bankCard = cashback.bankCard
            amount = cashback.roundedAmount
            measureUnit = cashback.measureUnit
            startDate = cashback.startDate
            expirationDate = cashback.expirationDate
            comment = cashback.comment
        }
    }


    override val errorMessage: String? get() = CashbackError.entries.firstNotNullOfOrNull { errorMessages[it] }


    fun updateStartDate(newDate: LocalDate?) {
        val newDate = newDate ?: Clock.System.today()
        ::startDate updateTo newDate
        expirationDate
            ?.takeIf { it < newDate }
            ?.let { ::expirationDate updateTo null }
    }

    fun updateAmount(newAmount: String) {
        val doubleAmount = newAmount.toDoubleOrNull()
        val isAmountCorrect = when {
            doubleAmount == null -> true
            measureUnit is MeasureUnit.Currency -> doubleAmount >= 0
            doubleAmount in 0.0..100.0 -> true
            else -> false
        }
        if (isAmountCorrect) {
            ::amount updateTo newAmount
        }
    }

    fun updateMeasureUnit(newUnit: MeasureUnit) {
        ::measureUnit updateTo newUnit
        if (
            newUnit is MeasureUnit.Percent &&
            amount.toDoubleOrNull()?.let { it !in 0.0..100.0 } == true
        ) {
            ::amount updateTo ""
        }
    }


    override fun updateErrorMessage(error: CashbackError, messageHandler: MessageHandler) {
        val message = when (error) {
            CashbackError.Owner -> {
                when (owner) {
                    null -> {
                        val exception = when (ownerType) {
                            CashbackOwnerType.Category -> CategoryNotSelectedException
                            CashbackOwnerType.Shop -> ShopNotSelectedException
                        }
                        messageHandler.getExceptionMessage(exception)
                    }
                    else -> null
                }
            }

            CashbackError.BankCard -> {
                val card = bankCard
                when {
                    card == null || card.id == 0L -> messageHandler
                        .getExceptionMessage(BankCardNotSelectedException)

                    else -> null
                }
            }
            CashbackError.Amount -> {
                val doubleAmount = amount.toDoubleOrNull()
                if (
                    doubleAmount == null ||
                    doubleAmount < 0 ||
                    (doubleAmount > 100 && measureUnit is MeasureUnit.Percent)
                ) {
                    messageHandler
                        .getExceptionMessage(IncorrectCashbackAmountException)
                }
                else {
                    null
                }
            }
        }

        message?.let { errorMessages[error] = it } ?: errorMessages.remove(error)
    }


    fun updateAllErrors(messageHandler: MessageHandler) {
        CashbackError.entries.forEach {
            updateErrorMessage(it, messageHandler)
        }
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


internal enum class CashbackError {
    Owner,
    BankCard,
    Amount
}