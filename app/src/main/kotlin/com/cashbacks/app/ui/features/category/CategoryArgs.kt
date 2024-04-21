package com.cashbacks.app.ui.features.category

internal sealed class CategoryArgs(
    val id: Long,
    val isEditing: Boolean,
    val startTab: TabItem = TabItem.Shops
) {
    class Editing(id: Long, startTab: TabItem = TabItem.Shops) : CategoryArgs(
        id = id,
        isEditing = true,
        startTab = startTab
    )
    class Viewing(id: Long, startTab: TabItem = TabItem.Shops) : CategoryArgs(
        id = id,
        isEditing = false,
        startTab = startTab
    )
}
