package com.cashbacks.app.ui.managment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface DialogType : Parcelable {

    @Parcelize
    data class ConfirmDeletion<T : Parcelable>(val value: T) : DialogType

    @Parcelize
    data object Save : DialogType
}