package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicInfoBankCard
import com.cashbacks.domain.model.PaymentSystem
import kotlin.reflect.KMutableProperty0

class ComposableBankCard(
    override val id: Long = 0,
    name: String = "",
    number: String = "",
    paymentSystem: PaymentSystem? = null,
    holder: String = "",
    validityPeriod: String = "",
    cvv: String = "",
    pin: String = "",
    comment: String = ""
) : BasicInfoBankCard {
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

    override var name by mutableStateOf(name)

    override var number by mutableStateOf(BankCardMapper.addSpacesToCardNumber(number))

    override var paymentSystem by mutableStateOf(paymentSystem)
    var holder by mutableStateOf(holder)
    var validityPeriod by mutableStateOf(validityPeriod)
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

    fun updateNumber(newNumber: String) {
        BankCardMapper
            .addSpacesToCardNumber(newNumber)
            .takeIf { it.length <= 19 }
            ?.let { updateValue(::number, it) }
    }

    fun updateValidityPeriod(newPeriod: String) {
        val newValue = when {
            newPeriod.length < 2 -> newPeriod
            newPeriod.length == 2 -> "$newPeriod / "
            newPeriod.length == 4 -> newPeriod.substring(0 .. 1)
            newPeriod.length <= 7 -> newPeriod
            else -> this.validityPeriod
        }
        updateValue(::validityPeriod, newValue)
    }

    fun mapToBankCard() = BankCard(
        id = this.id,
        name = this.name,
        number = BankCardMapper.removeSpacesFromNumber(number),
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = this.validityPeriod,
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment
    )
}