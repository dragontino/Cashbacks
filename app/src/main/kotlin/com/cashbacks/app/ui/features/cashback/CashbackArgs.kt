package com.cashbacks.app.ui.features.cashback

sealed class CashbackArgs(
    val cashbackId: Long?,
    val parentName: String?,
    val parentId: Long?
) {
    object New {
        class Category(cashbackId: Long?, categoryId: Long) : CashbackArgs(
            cashbackId = cashbackId,
            parentName = com.cashbacks.domain.model.Category::class.simpleName!!,
            parentId = categoryId
        )

        class Shop(cashbackId: Long?, shopId: Long) : CashbackArgs(
            cashbackId = cashbackId,
            parentName = com.cashbacks.domain.model.Shop::class.simpleName!!,
            parentId = shopId
        )
    }

    class Existing(id: Long) : CashbackArgs(
        cashbackId = id,
        parentName = null,
        parentId = null
    )
}
