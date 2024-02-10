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
import kotlin.reflect.KMutableProperty0

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
) {
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

    private val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    val haveChanges: Boolean get() = updatedProperties.isNotEmpty()

    fun <T> updateValue(property: KMutableProperty0<T>, newValue: T) {
        val previousValue = property.get()
        property.set(newValue)

        with(updatedProperties) {
            val changeHistory = this[property.name]

            when {
                changeHistory == null -> this[property.name] =
                    previousValue.toString() to newValue.toString()

                changeHistory.first == newValue.toString() -> remove(property.name)

                else -> this[property.name] = changeHistory.copy(second = newValue.toString())
            }
        }
    }

    fun updateNumber(newNumber: TextFieldValue) {
        val newText = when (newNumber.text.length) {
            5, 10, 15 -> buildString {
                append(newNumber.text.substring(0..<newNumber.text.length - 1))
                append(" ")
                append(newNumber.text.last())
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