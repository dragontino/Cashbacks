package com.cashbacks.domain.model

sealed interface BasicInfoCategory : ListItem {
    val name: String
}


data class BasicCategory(
    override val id: Long,
    override val name: String,
    val maxCashback: Cashback?
) : BasicInfoCategory


data class Category(
    override val id: Long,
    override val name: String,
    val shops: List<BasicShop> = listOf(),
    val cashbacks: List<Cashback> = listOf()
) : BasicInfoCategory
