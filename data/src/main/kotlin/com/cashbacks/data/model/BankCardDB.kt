package com.cashbacks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.FullBankCard
import com.cashbacks.domain.model.PaymentSystem
import com.cashbacks.domain.model.PrimaryBankCard

@Entity(tableName = "Cards")
data class BankCardDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: PaymentSystem?,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) {
    constructor(bankCard: FullBankCard) : this(
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

    fun mapToBankCard() = FullBankCard(
        id = id,
        name = name,
        number = number,
        paymentSystem = paymentSystem,
        holder = holder,
        validityPeriod = validityPeriod,
        cvv = cvv,
        pin = pin,
        comment = comment
    )
}


data class PrimaryBankCardDB(
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: PaymentSystem?,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
) {
    fun mapToDomainBankCard() = PrimaryBankCard(
        id = id,
        name = name,
        number = number,
        paymentSystem = paymentSystem,
        holder = holder,
        validityPeriod = validityPeriod,
        cvv = cvv
    )
}