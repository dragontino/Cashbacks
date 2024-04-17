package com.cashbacks.app.ui.features.category

internal data class CategoryArgs(
    val id: Long,
    val isEditing: Boolean,
    val startTab: TabItem = TabItem.Shops
)
