package com.cashbacks.app.ui.features.cashback

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CashbackArgs(
    val cashbackId: Long?,
    val ownerId: Long?,
    val ownerType: CashbackOwnerType,
) : Parcelable {

    companion object {
        fun fromCategory(categoryId: Long?) = CashbackArgs(
            cashbackId = null,
            ownerId = categoryId,
            ownerType = CashbackOwnerType.Category
        )

        fun fromCategory(cashbackId: Long, categoryId: Long) = CashbackArgs(
            cashbackId = cashbackId,
            ownerId = categoryId,
            ownerType = CashbackOwnerType.Category
        )


        fun fromShop(shopId: Long?) = CashbackArgs(
            cashbackId = null,
            ownerType = CashbackOwnerType.Shop,
            ownerId = shopId
        )

        fun fromShop(cashbackId: Long, shopId: Long) = CashbackArgs(
            cashbackId = cashbackId,
            ownerType = CashbackOwnerType.Shop,
            ownerId = shopId
        )
    }
}


enum class CashbackOwnerType {
    Category,
    Shop
}