package com.cashbacks.app.model

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.BasicInfoBankCard
import com.cashbacks.domain.model.Cashback

class ComposableCashback(
    val id: Long = 0,
    bankCard: BasicInfoBankCard = BasicBankCard(),
    amount: String = "",
    expirationDate: String? = "",
    comment: String = ""
) {
    constructor(cashback: Cashback) : this(
        id = cashback.id,
        bankCard = cashback.bankCard,
        amount = cashback.amount,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )

    val bankCard by derivedStateOf { bankCard }
    var amount by mutableStateOf(amount)
    var expirationDate by mutableStateOf(expirationDate ?: "")
    var comment by mutableStateOf(comment)

    fun mapToCashback() = Cashback(
        id = this.id,
        bankCard = bankCard,
        amount = this.amount,
        expirationDate = this.expirationDate,
        comment = this.comment
    )
}