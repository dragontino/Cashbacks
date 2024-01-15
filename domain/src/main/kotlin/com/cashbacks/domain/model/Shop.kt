package com.cashbacks.domain.model

sealed interface BasicInfoShop : ListItem {
    val name: String
}


data class Shop(
    override val id: Long,
    override val name: String = "",
    val cashbacks: List<Cashback> = listOf()
) : BasicInfoShop


data class BasicShop(
    override val id: Long,
    override val name: String,
    val maxCashback: Cashback?
) : BasicInfoShop
