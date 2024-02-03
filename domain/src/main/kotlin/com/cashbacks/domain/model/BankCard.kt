package com.cashbacks.domain.model

interface BasicInfoBankCard {
    val id: Long
    val name: String
    val number: String
    val paymentSystem: PaymentSystem?

    val lastFourDigitsOfNumber get() = with(number) {
        slice(length - 4 ..< length)
    }

    val hiddenNumber get() = with(number) {
        return@with when {
            length < 12 -> this
            else -> replaceRange(4 ..< 12, "********")
        }
    }
}


data class BasicBankCard(
    override val id: Long = 0,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null
) : BasicInfoBankCard


data class BankCard(
    override val id: Long = 0,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    val holder: String = "",
    val validityPeriod: String = "",
    val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) : BasicInfoBankCard
