package com.cashbacks.features.shop.presentation.api

import kotlinx.serialization.Serializable

@Serializable
data class ShopArgs(
    val shopId: Long?,
    val isEditing: Boolean
) {
    constructor() : this(shopId = null, isEditing = true)
    constructor(id: Long, isEditing: Boolean) : this(shopId = id, isEditing = isEditing)
}
