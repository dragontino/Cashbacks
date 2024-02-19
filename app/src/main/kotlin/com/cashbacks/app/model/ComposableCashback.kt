package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.cashbacks.domain.model.BankCard
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback

class ComposableCashback(
    val id: Long = 0,
    var bankCard: BankCard? = null,
    amount: String = "",
    expirationDate: String? = "",
    comment: String = ""
) : Updatable {
    constructor(cashback: Cashback) : this(
        id = cashback.id,
        bankCard = cashback.bankCard as BankCard,
        amount = cashback.amount,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )

    var amount by mutableStateOf(amount)
    var expirationDate by mutableStateOf(expirationDate ?: "")
    var comment by mutableStateOf(comment)


    override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    fun mapToCashback() = Cashback(
        id = this.id,
        bankCard = bankCard ?: BasicBankCard(),
        amount = this.amount,
        expirationDate = this.expirationDate.takeIf { it.isNotBlank() },
        comment = this.comment
    )
}