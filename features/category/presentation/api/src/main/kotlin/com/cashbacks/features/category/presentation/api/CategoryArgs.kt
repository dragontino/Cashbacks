package com.cashbacks.features.category.presentation.api

import kotlinx.serialization.Serializable

@Serializable
sealed class CategoryArgs {
    abstract val id: Long
    abstract val startTab: CategoryTabItemType

    @Serializable
    data class Viewing(
        override val id: Long,
        override val startTab: CategoryTabItemType = CategoryTabItemType.Cashbacks
    ) : CategoryArgs()

    @Serializable
    data class Editing(
        override val id: Long,
        override val startTab: CategoryTabItemType = CategoryTabItemType.Cashbacks
    ) : CategoryArgs()
}
