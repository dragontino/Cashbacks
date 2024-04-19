package com.cashbacks.app.ui.features.shop

sealed class ShopArgs(
    val shopId: Long?,
    val isEditing: Boolean
) {
    data object New : ShopArgs(shopId = null, isEditing = true)

    class Existing(id: Long, isEditing: Boolean) : ShopArgs(id, isEditing)
}