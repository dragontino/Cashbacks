package com.cashbacks.app.model

import android.content.res.Resources
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.cashbacks.app.ui.features.cashback.CashbackOwner
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.CashbackWithOwner
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.CategoryCashback
import com.cashbacks.domain.model.Shop
import com.cashbacks.domain.model.ShopCashback

sealed class ComposableCashback(
    val id: Long,
    var bankCard: BasicBankCard?,
    amount: String,
    expirationDate: String?,
    comment: String
) : Updatable {
    var amount by mutableStateOf(amount)
    var expirationDate by mutableStateOf(expirationDate ?: "")
    var comment by mutableStateOf(comment)

    private val _amountErrorMessage = mutableStateOf<String?>(null)
    val amountErrorMessage = derivedStateOf { _amountErrorMessage.value }

    private val _bankCardErrorMessage = mutableStateOf<String?>(null)
    val bankCardErrorMessage = derivedStateOf { _bankCardErrorMessage.value }

    protected val ownerErrorMessageInternal = mutableStateOf<String?>(null)
    val ownerErrorMessage = derivedStateOf { ownerErrorMessageInternal.value }

    final override val updatedProperties: SnapshotStateMap<String, Pair<String, String>> = mutableStateMapOf()

    val haveErrors: Boolean
        get() = ownerErrorMessage.value != null
                || amountErrorMessage.value != null
                || bankCardErrorMessage.value != null

    val errorMessage: String?
        get() = ownerErrorMessage.value ?: bankCardErrorMessage.value ?: amountErrorMessage.value


    abstract fun updateOwnerError(resources: Resources)


    fun updateAmountError(resources: Resources) {
        _amountErrorMessage.value = when {
            amount.toDoubleOrNull() == null -> resources.getString(R.string.incorrect_cashback_amount)
            else -> null
        }
    }

    fun updateBankCardError(resources: Resources) {
        _bankCardErrorMessage.value = when {
            bankCard == null || bankCard?.id == 0L ->
                resources.getString(R.string.bank_card_not_selected)
            else -> null
        }
    }

    fun updateErrors(resources: Resources) {
        updateOwnerError(resources)
        updateBankCardError(resources)
        updateAmountError(resources)
    }

    fun mapToCashback() = Cashback(
        id = this.id,
        bankCard = bankCard ?: BasicBankCard(),
        amount = this.amount,
        expirationDate = this.expirationDate.takeIf { it.isNotBlank() },
        comment = this.comment
    )
}


class ComposableCategoryCashback(
    id: Long = 0,
    var category: Category? = null,
    bankCard: BasicBankCard? = null,
    amount: String = "",
    expirationDate: String? = "",
    comment: String = ""
) : ComposableCashback(id, bankCard, amount, expirationDate, comment) {
    constructor(cashback: CategoryCashback) : this(
        id = cashback.id,
        category = cashback.category,
        bankCard = cashback.bankCard as BasicBankCard,
        amount = cashback.amount,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )

    override fun updateOwnerError(resources: Resources) {
        ownerErrorMessageInternal.value = when (category) {
            null -> resources.getString(R.string.category_not_selected)
            else -> null
        }
    }
}


class ComposableShopCashback(
    id: Long = 0,
    var shop: Shop? = null,
    bankCard: BasicBankCard? = null,
    amount: String = "",
    expirationDate: String? = "",
    comment: String = ""
) : ComposableCashback(id, bankCard, amount, expirationDate, comment) {
    constructor(cashback: ShopCashback) : this(
        id = cashback.id,
        shop = cashback.shop,
        bankCard = cashback.bankCard as BasicBankCard,
        amount = cashback.amount,
        expirationDate = cashback.expirationDate,
        comment = cashback.comment
    )

    override fun updateOwnerError(resources: Resources) {
        ownerErrorMessageInternal.value = when (shop) {
            null -> resources.getString(R.string.shop_not_selected)
            else -> null
        }
    }
}


fun ComposableCashback(cashback: CashbackWithOwner): ComposableCashback {
    return when (cashback) {
        is CategoryCashback -> ComposableCategoryCashback(cashback)
        is ShopCashback -> ComposableShopCashback(cashback)
    }
}


fun ComposableCashback(owner: CashbackOwner): ComposableCashback {
    return when (owner) {
        CashbackOwner.Category -> ComposableCategoryCashback()
        CashbackOwner.Shop -> ComposableShopCashback()
    }
}