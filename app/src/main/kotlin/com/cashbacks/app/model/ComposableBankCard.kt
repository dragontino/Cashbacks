package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicInfoBankCard
import com.cashbacks.domain.model.PaymentSystem

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

    private var cardNumberWithoutSpaces = number

    override var number by mutableStateOf(addSpacesToNumber(number))
        private set

    override var paymentSystem by mutableStateOf(paymentSystem)
    var holder by mutableStateOf(holder)
    var validityPeriod by mutableStateOf(validityPeriod)
        private set

    var cvv by mutableStateOf(cvv)
    var pin by mutableStateOf(pin)
    var comment by mutableStateOf(comment)

    fun updateNumber(newNumber: String) {
        val oldNumber = this.number
        if (oldNumber.length < newNumber.length) {
            if (cardNumberWithoutSpaces.length < 16) {
                val newChar = newNumber.last()
                if (newChar.isDigit()) cardNumberWithoutSpaces += newChar
                if (newChar.isDigit() || newChar == ' ') this.number = newNumber
            }
        }
        else {
            val removedChar = oldNumber.last()
            if (removedChar.isDigit()) {
                cardNumberWithoutSpaces = cardNumberWithoutSpaces.removeSuffix(removedChar.toString())
            }
            this.number = newNumber
        }
    }

    fun updateValidityPeriod(newPeriod: String) {
        this.validityPeriod = when {
            newPeriod.length <= 2 -> "$newPeriod / "
            newPeriod.length == 4 -> newPeriod.substring(0 .. 1)
            newPeriod.length <= 7 -> newPeriod
            else -> this.validityPeriod
        }
    }

    fun mapToBankCard() = BankCard(
        id = this.id,
        name = this.name,
        number = this.cardNumberWithoutSpaces,
        paymentSystem = this.paymentSystem,
        holder = this.holder,
        validityPeriod = this.validityPeriod,
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment
    )
}