package com.cashbacks.features.bankcard.presentation.impl.mvi.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.domain.model.PaymentSystem
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Immutable
data class EditableBankCard(
    val id: Long? = null,
    val name: String = "",
    val number: TextFieldValue = TextFieldValue(""),
    val paymentSystem: PaymentSystem? = null,
    val holder: String = "",
    val validityPeriod: LocalDate? = null,
    val cvv: String = "",
    val pin: String = "",
    val maxCashbacksNumber: Int? = null,
    val comment: String = ""
) {
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


    constructor(card: FullBankCard) : this(
        id = card.id,
        name = card.name,
        number = TextFieldValue(card.number.withSpaces()),
        paymentSystem = card.paymentSystem,
        holder = card.holder,
        validityPeriod = card.validityPeriod
            .takeIf { it.isNotBlank() }
            ?.let(::parseValidityPeriodToDate),
        cvv = card.cvv,
        pin = card.pin,
        maxCashbacksNumber = card.maxCashbacksNumber,
        comment = card.comment
    )


    fun updateNumber(newNumber: TextFieldValue): EditableBankCard {
        if (newNumber.text.length > number.text.length && !newNumber.text.last().isDigit()) {
            return this
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

        return copy(
            number = newNumber,
            paymentSystem = getPaymentSystemByNumber(newText)
        )
    }

    private fun getPaymentSystemByNumber(number: String): PaymentSystem? {
        val withoutSpacesNumber = BankCardUtils.removeSpacesFromNumber(number)
        return PaymentSystem.entries
            .find { withoutSpacesNumber.startsWith(it.prefix) }
            ?: paymentSystem
    }


    fun mapToBankCard() = FullBankCard(
        id = this.id ?: 0L,
        name = this.name,
        number = BankCardUtils.removeSpacesFromNumber(number.text),
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = this.validityPeriod?.let(::formatDateToValidityPeriod) ?: "",
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment,
        maxCashbacksNumber = this.maxCashbacksNumber
    )
}