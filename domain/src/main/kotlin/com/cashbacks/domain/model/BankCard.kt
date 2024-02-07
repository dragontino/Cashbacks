package com.cashbacks.domain.model

interface BasicInfoBankCard {
    val id: Long
    val name: String
    val number: String
    val paymentSystem: PaymentSystem?

    val hiddenLastDigitsOfNumber get() = "${getHidden(4)} $lastFourDigitsOfNumber"

    val lastFourDigitsOfNumber get() = with(number) {
        slice(length - 4 ..< length)
    }

    val hiddenNumber get() = with(number) {
        return@with when {
            length < 12 -> this
            else -> replaceRange(4..<12, replacement = getHidden(length = 8))
        }
    }

    fun getHidden(length: Int, mask: Char = '\u2022') = buildString {
        repeat(length) {
            append(mask)
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
) : BasicInfoBankCard {
    override fun toString(): String {
        return "$name $hiddenLastDigitsOfNumber $validityPeriod"
    }
}
