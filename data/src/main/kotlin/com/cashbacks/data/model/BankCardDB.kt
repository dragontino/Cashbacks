package com.cashbacks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.PaymentSystem

@Entity(tableName = "Cards")
data class BankCardDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: String?,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) {
    constructor(bankCard: BankCard) : this(
        id = bankCard.id,
        name = bankCard.name,
        number = bankCard.number,
        paymentSystem = bankCard.paymentSystem?.name,
        holder = bankCard.holder,
        validityPeriod = bankCard.validityPeriod,
        cvv = bankCard.cvv,
        pin = bankCard.pin,
        comment = bankCard.comment
    )

    fun mapToBankCard() = BankCard(
        id = id,
        name = name,
        number = number,
        paymentSystem = paymentSystem?.let { PaymentSystem.valueOf(it) },
        holder = holder,
        validityPeriod = validityPeriod,
        cvv = cvv,
        pin = pin,
        comment = comment
    )
}