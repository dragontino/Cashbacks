package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cashbacks.app.util.BankCardUtils
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.PaymentSystem
import kotlin.math.abs
import kotlin.random.Random

class ComposableBankCard(
    id: Long? = null,
    name: String = "",
    number: String = "",
    paymentSystem: PaymentSystem? = null,
    holder: String = "",
    validityPeriod: String = "",
    cvv: String = "",
    pin: String = "",
    comment: String = ""
) : Updatable {

    var id by mutableStateOf(id)
    var name by mutableStateOf(name)
    var number by mutableStateOf(TextFieldValue(BankCardUtils.addSpacesToCardNumber(number)))
        private set

    var paymentSystem by mutableStateOf(paymentSystem)
    var holder by mutableStateOf(holder)
    var validityPeriod by mutableStateOf(TextFieldValue(validityPeriod))
        private set

    var cvv by mutableStateOf(cvv)
    var pin by mutableStateOf(pin)
    var comment by mutableStateOf(comment)

    override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    fun update(card: FullBankCard) {
        id = card.id
        name = card.name
        number = TextFieldValue(card.number)
        paymentSystem = card.paymentSystem
        holder = card.holder
        validityPeriod = TextFieldValue(card.validityPeriod)
        cvv = card.cvv
        pin = card.pin
        comment = card.comment
    }


    fun updateNumber(newNumber: TextFieldValue) {
        if (newNumber.text.length > number.text.length && !newNumber.text.last().isDigit()) {
            return
        }

        val newText = if (abs(newNumber.text.length - number.text.length) > 1) {
            BankCardUtils.addSpacesToCardNumber(newNumber.text)
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

        ::number updateTo newNumber.copy(
            text = newText,
            selection = TextRange(newText.length)
        )

        updatePaymentSystemByNumber(newText)
    }

    private fun updatePaymentSystemByNumber(number: String) {
        val withoutSpacesNumber = BankCardUtils.removeSpacesFromNumber(number)
        val newPaymentSystem = PaymentSystem.entries.find { withoutSpacesNumber.startsWith(it.prefix) }

        if (newPaymentSystem != null) {
            ::paymentSystem updateTo newPaymentSystem
        }
    }


    fun updateValidityPeriod(newPeriod: TextFieldValue) {
        val newText = when {
            newPeriod.text.length < 2 -> newPeriod.text
            newPeriod.text.length == 2 -> "${newPeriod.text} / "
            newPeriod.text.length == 4 -> newPeriod.text.substring(0 .. 1)
            newPeriod.text.length <= 7 -> newPeriod.text
            else -> this.validityPeriod.text
        }
        ::validityPeriod updateTo newPeriod.copy(
                text = newText,
                selection = TextRange(newText.length)
            )
    }

    fun mapToBankCard() = FullBankCard(
        id = this.id ?: Random.nextLong(),
        name = this.name,
        number = BankCardUtils.removeSpacesFromNumber(number.text),
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = this.validityPeriod.text,
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment
    )
}