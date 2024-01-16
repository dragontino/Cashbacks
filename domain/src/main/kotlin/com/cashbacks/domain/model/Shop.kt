package com.cashbacks.domain.model


data class Shop(
    val id: Long,
    val name: String,
    val maxCashback: Cashback?
)
