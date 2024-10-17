package com.cashbacks.app.ui.features.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopArgs internal constructor(
    val shopId: Long?,
    val isEditing: Boolean
) : Parcelable {
    constructor() : this(shopId = null, isEditing = true)

    constructor(id: Long, isEditing: Boolean) : this(shopId = id, isEditing = isEditing)
}