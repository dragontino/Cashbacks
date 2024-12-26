package com.cashbacks.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

sealed interface BasicBankCard : Parcelable {
    val id: Long
    val name: String
    val number: String
    val paymentSystem: PaymentSystem?
    val maxCashbacksNumber: Int?
}


sealed interface BankCard : BasicBankCard {
    val holder: String
    val validityPeriod: String
    val cvv: String
}




@Immutable
@Parcelize
data class PreviewBankCard(
    override val id: Long = 0,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val maxCashbacksNumber: Int? = null
) : BasicBankCard


@Immutable
@Parcelize
data class PrimaryBankCard(
    override val id: Long = 0L,
    override val name: String = "",
    override val number: String = "",
    override val paymentSystem: PaymentSystem? = null,
    override val holder: String = "",
    override val validityPeriod: String = "",
    override val cvv: String = "",
    override val maxCashbacksNumber: Int? = null
) : BankCard {
    fun getBasicInfo() = PreviewBankCard(id, name, number, paymentSystem)
}


@Immutable
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
    override val maxCashbacksNumber: Int? = null
) : BankCard
