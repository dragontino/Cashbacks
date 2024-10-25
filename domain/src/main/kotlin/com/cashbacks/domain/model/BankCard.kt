package com.cashbacks.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface BasicBankCard : Parcelable {
    val id: Long
    val name: String
    val number: String
    val paymentSystem: PaymentSystem?

    val hiddenLastDigitsOfNumber get() = "${getHidden(4)} $lastFourDigitsOfNumber"

    val lastFourDigitsOfNumber get() = with(number) {
        when {
            length < 4 -> this
            else -> slice(length - 4 ..< length)
        }

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


sealed interface BankCard : BasicBankCard {
    val holder: String
    val validityPeriod: String
    val cvv: String
}


@Parcelize
data class PreviewBankCard(
    override val id: Long = 0,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null
) : BasicBankCard {
    override fun toString(): String {
        return "$name $hiddenLastDigitsOfNumber"
    }
}


@Parcelize
data class PrimaryBankCard(
    override val id: Long = 0L,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val holder: String = "",
    override val validityPeriod: String = "",
    override val cvv: String = ""
) : BankCard {
    fun getBasicInfo() = PreviewBankCard(id, name, number, paymentSystem)
}


@Parcelize
data class FullBankCard(
    override val id: Long = 0L,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val holder: String = "",
    override val validityPeriod: String = "",
    override val cvv: String = "",
    val pin: String = "",
    val comment: String = "",
) : BankCard
