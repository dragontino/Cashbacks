package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BankCard

class ComposableBankCard(
    private val id: Long = 1,
    name: String = "",
    number: String = "",
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
        holder = bankCard.holder,
        validityPeriod = bankCard.validityPeriod,
        cvv = bankCard.cvv,
        pin = bankCard.pin,
        comment = bankCard.comment
    )

    var name by mutableStateOf(name)
    var number by mutableStateOf(number)
    var holder by mutableStateOf(holder)
    var validityPeriod by mutableStateOf(validityPeriod)
    var cvv by mutableStateOf(cvv)
    var pin by mutableStateOf(pin)
    var comment by mutableStateOf(comment)

    fun mapToBankCard() = BankCard(
        id = this.id,
        name = this.name,
        number = this.number,
        holder = this.holder,
        validityPeriod = this.validityPeriod,
        cvv = this.cvv,
        pin = this.pin,
        comment = this.comment
    )
}