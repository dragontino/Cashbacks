package com.cashbacks.app.ui.managment

sealed interface DialogType {
    data class ConfirmDeletion<T : Any>(val value: T) : DialogType

    data object Save : DialogType
}