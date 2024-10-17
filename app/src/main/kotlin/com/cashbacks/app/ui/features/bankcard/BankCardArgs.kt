package com.cashbacks.app.ui.features.bankcard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankCardArgs internal constructor(
    val cardId: Long?,
    val isEditing: Boolean
) : Parcelable {

    constructor() : this(cardId = null, isEditing = true)

    constructor(id: Long, isEditing: Boolean) : this(cardId = id, isEditing = isEditing)
}