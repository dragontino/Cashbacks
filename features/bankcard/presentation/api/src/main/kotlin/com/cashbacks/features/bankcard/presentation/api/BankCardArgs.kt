package com.cashbacks.features.bankcard.presentation.api

import kotlinx.serialization.Serializable

sealed class BankCardArgs {
    abstract val cardId: Long?

    @Serializable
    data class Viewing(override val cardId: Long) : BankCardArgs()

    @Serializable
    data class Editing(override val cardId: Long?) : BankCardArgs()
}

fun emptyBankCardArgs(): BankCardArgs = BankCardArgs.Editing(cardId = null)