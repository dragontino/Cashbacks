package com.cashbacks.features.bankcard.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class BasicBankCard {
    abstract val id: Long
    abstract val name: String
    abstract val number: String
    abstract val paymentSystem: PaymentSystem?
    abstract val maxCashbacksNumber: Int?
}


@Serializable
sealed class BankCard : BasicBankCard() {
    abstract val holder: String
    abstract val validityPeriod: String
    abstract val cvv: String
}


@Serializable
data class PreviewBankCard(
    override val id: Long = 0,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val maxCashbacksNumber: Int? = null
) : BasicBankCard()


@Serializable
data class PrimaryBankCard(
    override val id: Long = 0L,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val holder: String = "",
    override val validityPeriod: String = "",
    override val cvv: String = "",
    override val maxCashbacksNumber: Int? = null
) : BankCard()


@Serializable
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
    override val maxCashbacksNumber: Int? = null
) : BankCard()
