package com.cashbacks.app.ui.features.bankcard

sealed class BankCardArgs(
    val id: Long?,
    val isEditing: Boolean
) {
    data object New : BankCardArgs(id = null, isEditing = true)
    class Existing(id: Long, isEditing: Boolean) : BankCardArgs(id, isEditing)
}