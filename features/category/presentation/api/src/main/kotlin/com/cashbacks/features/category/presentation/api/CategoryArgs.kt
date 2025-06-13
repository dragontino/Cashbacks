package com.cashbacks.features.category.presentation.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface CategoryArgs {
    val id: Long
    val startTab: CategoryTabItemType

    @Serializable
    data class Viewing(
        override val id: Long,
        override val startTab: CategoryTabItemType = CategoryTabItemType.Cashbacks
    ) : CategoryArgs

    @Serializable
    data class Editing(
        override val id: Long,
        override val startTab: CategoryTabItemType = CategoryTabItemType.Cashbacks
    ) : CategoryArgs
}
