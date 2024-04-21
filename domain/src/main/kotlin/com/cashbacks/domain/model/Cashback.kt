package com.cashbacks.domain.model

import android.content.res.Resources
import com.cashbacks.domain.R

sealed interface CashbackInterface {
    val id: Long
    val bankCard: BasicInfoBankCard
    val amount: String
    val expirationDate: String?
    val comment: String

    val roundedAmount: String get() = amount
        .toDoubleOrNull()
        ?.takeIf { it % 1 == 0.0 }
        ?.toInt()?.toString()
        ?: amount
}


data class Cashback(
    override val id: Long,
    override val bankCard: BasicInfoBankCard,
    override val amount: String,
    override val expirationDate: String?,
    override val comment: String
) : CashbackInterface



sealed class CashbackWithOwner(
    override val id: Long,
    val ownerName: String,
    override val bankCard: BasicInfoBankCard,
    override val amount: String,
    override val expirationDate: String?,
    override val comment: String
) : CashbackInterface {
    fun asCashback() = Cashback(id, bankCard, amount, expirationDate, comment)

    abstract fun getParentType(resources: Resources): String
}


class ShopCashback(
    id: Long,
    val shop: Shop,
    bankCard: BasicInfoBankCard,
    amount: String,
    expirationDate: String?,
    comment: String
) : CashbackWithOwner(
    id = id,
    ownerName = shop.name,
    bankCard = bankCard,
    amount = amount,
    expirationDate = expirationDate,
    comment = comment
) {
    override fun getParentType(resources: Resources): String {
        return resources.getString(R.string.shop)
    }
}


class CategoryCashback(
    id: Long,
    val category: Category,
    bankCard: BasicInfoBankCard,
    amount: String,
    expirationDate: String?,
    comment: String
) : CashbackWithOwner(
    id = id,
    ownerName = category.name,
    bankCard = bankCard,
    amount = amount,
    expirationDate = expirationDate,
    comment = comment
) {
    override fun getParentType(resources: Resources): String {
        return resources.getString(R.string.category_title)
    }
}