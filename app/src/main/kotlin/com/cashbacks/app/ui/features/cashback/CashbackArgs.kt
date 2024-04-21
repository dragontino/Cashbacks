package com.cashbacks.app.ui.features.cashback

import android.content.res.Resources
import com.cashbacks.domain.R

sealed class CashbackArgs(
    val cashbackId: Long?,
    val ownerType: CashbackOwner,
    val ownerId: Long?
) {

    object Category {
        class New(categoryId: Long?) : CashbackArgs(
            cashbackId = null,
            ownerType = CashbackOwner.Category,
            ownerId = categoryId,
        )

        class Existing(cashbackId: Long, categoryId: Long) : CashbackArgs(
            cashbackId = cashbackId,
            ownerType = CashbackOwner.Category,
            ownerId = categoryId
        )
    }

    object Shop {
        class New(shopId: Long?) : CashbackArgs(
            cashbackId = null,
            ownerType = CashbackOwner.Shop,
            ownerId = shopId
        )

        class Existing(cashbackId: Long, shopId: Long) : CashbackArgs(
            cashbackId = cashbackId,
            ownerType = CashbackOwner.Shop,
            ownerId = shopId
        )
    }
}


enum class CashbackOwner {
    Category {
        override fun getTitle(resources: Resources) = resources.getString(R.string.category_title)
    },
    Shop {
        override fun getTitle(resources: Resources) = resources.getString(R.string.shop)
    };

    abstract fun getTitle(resources: Resources): String
}