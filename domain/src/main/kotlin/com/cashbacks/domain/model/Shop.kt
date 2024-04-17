package com.cashbacks.domain.model

data class Shop(
    val id: Long,
    val name: String,
    val maxCashback: Cashback?
)


data class CategoryShop(
    val id: Long,
    val parentCategory: Category,
    val name: String,
    val maxCashback: Cashback?
) {
    fun asShop() = Shop(id, name, maxCashback)
}
