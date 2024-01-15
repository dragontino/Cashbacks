package com.cashbacks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.PaymentSystem

@Entity(tableName = "Cards")
data class BankCardDB(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: String,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) {
    fun mapToBankCard() = BankCard(
        id = id,
        name = name,
        number = number,
        paymentSystem = PaymentSystem.valueOf(paymentSystem),
        holder = holder,
        validityPeriod = validityPeriod,
        cvv = cvv,
        pin = pin,
        comment = comment
    )
}


data class BasicBankCardDB(
    val id: Long,
    val name: String,
    val number: String,
    val paymentSystem: String
) {
    fun mapToBankCard() = BasicBankCard(
        id = id,
        name = name,
        number = number,
        paymentSystem = PaymentSystem.valueOf(paymentSystem)
    )
}