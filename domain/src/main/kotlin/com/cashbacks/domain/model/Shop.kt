package com.cashbacks.domain.model

sealed interface ShopInterface {
    val id: Long
    val name: String
    val maxCashback: Cashback?
}

data class Shop(
    override val id: Long,
    override val name: String,
    override val maxCashback: Cashback?
) : ShopInterface


data class CategoryShop(
    override val id: Long,
    val parentCategory: Category,
    override val name: String,
    override val maxCashback: Cashback?
) : ShopInterface {
    fun asShop() = Shop(id, name, maxCashback)
}
