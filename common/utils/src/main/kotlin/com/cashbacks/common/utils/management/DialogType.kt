package com.cashbacks.common.utils.management

import kotlinx.serialization.Serializable

interface DialogType {

    @Serializable
    data class ConfirmDeletion<T>(val value: T) : DialogType

    @Serializable
    data object Save : DialogType
}