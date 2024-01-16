package com.cashbacks.domain.model

data class Category(
    val id: Long,
    val name: String,
    val maxCashback: Cashback?
)
