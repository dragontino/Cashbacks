package com.cashbacks.features.cashback.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class CashbackOwner {
    abstract val id: Long
    abstract val name: String


    @Serializable
    data class Category(
        override val id: Long,
        override val name: String
    ) : CashbackOwner()


    sealed class Shop : CashbackOwner()

    @Serializable
    data class BasicShop(
        override val id: Long,
        override val name: String
    ) : Shop()

    data class CategoryShop(
        override val id: Long,
        override val name: String,
        val parent: com.cashbacks.features.category.domain.model.Category
    ) : Shop()
}

