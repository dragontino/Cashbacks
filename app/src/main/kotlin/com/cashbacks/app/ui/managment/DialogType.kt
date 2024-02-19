package com.cashbacks.app.ui.managment

import java.time.LocalDate

sealed interface DialogType {
    data class ConfirmDeletion<T : Any>(val value: T) : DialogType

    data object Save : DialogType

    data class DatePicker(val date: LocalDate?) : DialogType
}