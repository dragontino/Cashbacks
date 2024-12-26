package com.cashbacks.app.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cashbacks.app.util.BankCardUtils
import com.cashbacks.app.util.BankCardUtils.withSpaces
import com.cashbacks.domain.model.EmptyCardValidityPeriodException
import com.cashbacks.domain.model.EmptyPinCodeException
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.IncorrectCardCvvException
import com.cashbacks.domain.model.IncorrectCardNumberException
import com.cashbacks.domain.model.MessageHandler
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.util.today
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Stable
internal class ComposableBankCard(
    id: Long? = null,
    name: String = "",
    number: String = "",
    paymentSystem: PaymentSystem? = null,
    holder: String = "",
    validityPeriod: String? = null,
    cvv: String = "",
    pin: String = "",
    maxCashbacksNumber: Int? = null,
    comment: String = ""
) : Updatable, ErrorHolder<BankCardError> {

    companion object {
        const val VALIDITY_PERIOD_DATE_PATTERN = "MM / uu"

        private const val VALIDITY_PERIOD_DATE_PATTERN_WITH_DAY = "dd / $VALIDITY_PERIOD_DATE_PATTERN"

        private fun parseValidityPeriodToDate(validityPeriod: String): LocalDate {
            val formatter = DateTimeFormatter.ofPattern(VALIDITY_PERIOD_DATE_PATTERN_WITH_DAY)
            val validityPeriod = "01 / ${validityPeriod.trimStart()}"
            return java.time.LocalDate.parse(validityPeriod, formatter).toKotlinLocalDate()
        }

        private fun formatDateToValidityPeriod(date: LocalDate): String {
            val formatter = DateTimeFormatter.ofPattern(VALIDITY_PERIOD_DATE_PATTERN)
            return date.toJavaLocalDate().format(formatter)
        }
    }


    var id by mutableStateOf(id)
    var name by mutableStateOf(name)
    var number by mutableStateOf(TextFieldValue(number.withSpaces()))
        private set

    var paymentSystem by mutableStateOf(paymentSystem)
    var holder by mutableStateOf(holder)
    var validityPeriod by validityPeriod
        ?.takeIf { it.isNotBlank() }
        ?.let(::parseValidityPeriodToDate)
        .let { mutableStateOf(it) }

    var cvv by mutableStateOf(cvv)
    var pin by mutableStateOf(pin)
    var maxCashbacksNumber by mutableStateOf(maxCashbacksNumber)
    var comment by mutableStateOf(comment)

    override val updatedProperties = mutableStateMapOf<String, Pair<String, String>>()

    private val errorMessages = mutableStateMapOf<BankCardError, String>()
    override val errors: Map<BankCardError, String> get() = errorMessages.toMap()

    fun update(card: FullBankCard) {
        id = card.id
        name = card.name
        number = TextFieldValue(card.number.withSpaces())
        paymentSystem = card.paymentSystem
        holder = card.holder
        validityPeriod = card.validityPeriod
            .takeIf { it.isNotBlank() }
            ?.let(::parseValidityPeriodToDate)
        cvv = card.cvv
        pin = card.pin
        maxCashbacksNumber = card.maxCashbacksNumber
        comment = card.comment
    }

    override val errorMessage: String? get() = BankCardError.entries.firstNotNullOfOrNull { errorMessages[it] }


    fun updateNumber(newNumber: TextFieldValue) {
        if (newNumber.text.length > number.text.length && !newNumber.text.last().isDigit()) {
            return
        }

        val newText = if (abs(newNumber.text.length - number.text.length) > 1) {
            newNumber.text.withSpaces()
        } else when (newNumber.text.length) {
            4, 9, 14 -> {
                if (newNumber.text.length < number.text.length) {
                    with(newNumber.text) { substring(0..<lastIndex) }
                } else buildString {
                    append(newNumber.text, " ")
                }
            }
            5, 10, 15 -> {
                if (newNumber.text.length < number.text.length) {
                    with(newNumber.text) { substring(0..<lastIndex) }
                } else buildString {
                    append(newNumber.text.substring(0..<newNumber.text.lastIndex))
                    append(" ")
                    append(newNumber.text.last())
                }
            }
            20 -> number.text
            else -> newNumber.text
        }

        val newNumber = newNumber.copy(
            text = newText,
            selection = TextRange(newText.length)
        )
        when (number.text) {
            newText -> number = newNumber
            else -> ::number updateTo newNumber
        }

        updatePaymentSystemByNumber(newText)
    }

    private fun updatePaymentSystemByNumber(number: String) {
        val withoutSpacesNumber = BankCardUtils.removeSpacesFromNumber(number)
        val newPaymentSystem = PaymentSystem.entries.find { withoutSpacesNumber.startsWith(it.prefix) }

        if (newPaymentSystem != null && paymentSystem != newPaymentSystem) {
            ::paymentSystem updateTo newPaymentSystem
        }
    }


    override fun updateErrorMessage(error: BankCardError, handler: MessageHandler) {
        val message = when (error) {
            BankCardError.Number -> BankCardUtils
                .removeSpacesFromNumber(number.text)
                .takeIf { it.length < 16 }
                ?.let { handler.getExceptionMessage(IncorrectCardNumberException) }

            BankCardError.ValidityPeriod -> when (validityPeriod) {
                null -> handler.getExceptionMessage(EmptyCardValidityPeriodException)
                else -> null
            }

            BankCardError.Cvv -> cvv.takeIf { it.length < 3 }?.let {
                handler.getExceptionMessage(IncorrectCardCvvException)
            }

            BankCardError.Pin -> pin.takeIf { it.isBlank() }?.let {
                handler.getExceptionMessage(EmptyPinCodeException)
            }
        }

        message?.let { errorMessages[error] = it } ?: errorMessages.remove(error)
    }


    fun updateAllErrors(handler: MessageHandler) {
        BankCardError.entries.forEach {
            updateErrorMessage(it, handler)
        }
    }


    fun mapToBankCard() = FullBankCard(
        id = this.id ?: 0L,
        name = this.name,
        number = BankCardUtils.removeSpacesFromNumber(number.text),
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = (this.validityPeriod ?: Clock.System.today()).let(::formatDateToValidityPeriod),
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment,
        maxCashbacksNumber = this.maxCashbacksNumber
    )
}


internal enum class BankCardError {
    Number,
    ValidityPeriod,
    Cvv,
    Pin
}