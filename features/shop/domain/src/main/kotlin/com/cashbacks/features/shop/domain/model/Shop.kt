package com.cashbacks.features.shop.domain.model

import com.cashbacks.features.category.domain.model.Category
import kotlinx.serialization.Serializable

sealed interface Shop {
    val id: Long
    val name: String
}


@Serializable
data class BasicShop(
    override val id: Long = 0L,
    override val name: String = ""
) : Shop


@Serializable
data class CategoryShop(
    override val id: Long = 0L,
    override val name: String = "",
    val parent: Category = Category()
) : Shop