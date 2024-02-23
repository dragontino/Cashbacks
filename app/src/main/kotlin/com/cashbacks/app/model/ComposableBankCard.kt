package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.PaymentSystem
import kotlin.math.abs

class ComposableBankCard(
    val id: Long = 0,
    name: String = "",
    number: String = "",
    paymentSystem: PaymentSystem? = null,
    holder: String = "",
    validityPeriod: String = "",
    cvv: String = "",
    pin: String = "",
    comment: String = ""
) : Updatable {
    constructor(bankCard: BankCard) : this(
        id = bankCard.id,
        name = bankCard.name,
        number = bankCard.number,
        paymentSystem = bankCard.paymentSystem,
        holder = bankCard.holder,
        validityPeriod = bankCard.validityPeriod,
        cvv = bankCard.cvv,
        pin = bankCard.pin,
        comment = bankCard.comment
    )

    var name by mutableStateOf(name)
    var number by mutableStateOf(TextFieldValue(BankCardMapper.addSpacesToCardNumber(number)))
        private set

    var paymentSystem by mutableStateOf(paymentSystem)
    var holder by mutableStateOf(holder)
    var validityPeriod by mutableStateOf(TextFieldValue(validityPeriod))
        private set

    var cvv by mutableStateOf(cvv)
    var pin by mutableStateOf(pin)
    var comment by mutableStateOf(comment)

    override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    fun updateNumber(newNumber: TextFieldValue) {
        if (newNumber.text.length > number.text.length && !newNumber.text.last().isDigit()) {
            return
        }

        println("new length = ${newNumber.text.length}, old length = ${number.text.length}")
        val newText = if (abs(newNumber.text.length - number.text.length) > 1) {
            BankCardMapper.addSpacesToCardNumber(newNumber.text)
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
        updateValue(
            property = ::number,
            newValue = newNumber.copy(
                text = newText,
                selection = TextRange(newText.length)
            )
        )
    }

    fun updateValidityPeriod(newPeriod: TextFieldValue) {
        val newText = when {
            newPeriod.text.length < 2 -> newPeriod.text
            newPeriod.text.length == 2 -> "${newPeriod.text} / "
            newPeriod.text.length == 4 -> newPeriod.text.substring(0 .. 1)
            newPeriod.text.length <= 7 -> newPeriod.text
            else -> this.validityPeriod.text
        }
        updateValue(
            property = ::validityPeriod,
            newValue = newPeriod.copy(
                text = newText,
                selection = TextRange(newText.length)
            )
        )
    }

    fun mapToBankCard() = BankCard(
        id = this.id,
        name = this.name,
        number = BankCardMapper.removeSpacesFromNumber(number.text),
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = this.validityPeriod.text,
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment
    )
}