package com.cashbacks.common.composables.management

import androidx.compose.runtime.Stable

@Stable
interface DialogType {
    data class ConfirmDeletion<T>(val value: T) : DialogType
    data object Save : DialogType
}