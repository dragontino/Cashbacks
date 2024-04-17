package com.cashbacks.app.model

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.cashbacks.domain.R
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

    private val _amountErrorMessage = mutableStateOf("")
    val amountErrorMessage = derivedStateOf { _amountErrorMessage.value }

    private val _bankCardErrorMessage = mutableStateOf("")
    val bankCardErrorMessage = derivedStateOf { _bankCardErrorMessage.value }

    override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    val haveErrors: Boolean
        get() = amountErrorMessage.value.isNotBlank()
                || bankCardErrorMessage.value.isNotBlank()

    val errorMessage: String?
        get() = bankCardErrorMessage.value.takeIf { it.isNotBlank() }
            ?: amountErrorMessage.value.takeIf { it.isNotBlank() }


    fun updateAmountError(context: Context) {
        _amountErrorMessage.value = when {
            amount.toDoubleOrNull() == null -> context.getString(R.string.incorrect_cashback_amount)
            else -> ""
        }
    }

    fun updateBankCardError(context: Context) {
        _bankCardErrorMessage.value = when {
            bankCard == null || bankCard?.id == 0L ->
                context.getString(R.string.bank_card_not_selected)
            else -> ""
        }
    }

    fun updateErrors(context: Context) {
        updateAmountError(context)
        updateBankCardError(context)
    }

    fun mapToCashback() = Cashback(
        id = this.id,
        bankCard = bankCard ?: BasicBankCard(),
        amount = this.amount,
        expirationDate = this.expirationDate.takeIf { it.isNotBlank() },
        comment = this.comment
    )
}