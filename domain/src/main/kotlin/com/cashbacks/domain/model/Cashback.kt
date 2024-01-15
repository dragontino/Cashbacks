package com.cashbacks.domain.model

data class Cashback(
    override val id: Long,
    val bankCard: BasicInfoBankCard,
    val amount: String,
    val expirationDate: String?,
    val comment: String
) : ListItem