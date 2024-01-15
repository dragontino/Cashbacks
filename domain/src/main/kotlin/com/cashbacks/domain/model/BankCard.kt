package com.cashbacks.domain.model

sealed interface BasicInfoBankCard {
    val id: Long
    val name: String
    val number: String
    val paymentSystem: PaymentSystem

    val hiddenNumber get() = with(number) {
        slice(length - 4 ..< length)
    }
}


data class BasicBankCard(
    override val id: Long,
    override val name: String,
    override val number: String,
    override val paymentSystem: PaymentSystem
) : BasicInfoBankCard


data class BankCard(
    override val id: Long,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem = PaymentSystem.MasterCard,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) : BasicInfoBankCard
