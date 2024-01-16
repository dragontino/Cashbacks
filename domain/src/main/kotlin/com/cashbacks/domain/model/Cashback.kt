package com.cashbacks.domain.model

data class Cashback(
    val id: Long,
    val bankCard: BasicInfoBankCard,
    val amount: String,
    val expirationDate: String?,
    val comment: String
)