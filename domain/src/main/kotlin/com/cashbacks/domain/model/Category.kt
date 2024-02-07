package com.cashbacks.domain.model

data class Category(
    val id: Long = 0,
    val name: String = "",
    val maxCashback: Cashback? = null
)
