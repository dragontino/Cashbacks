package com.cashbacks.features.cashback.domain.utils

import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop

fun Category.asCashbackOwner() = CashbackOwner.Category(
    id = id,
    name = name
)

fun Shop.asCashbackOwner() = CashbackOwner.BasicShop(
    id = id,
    name = name
)

fun CategoryShop.asCashbackOwner() = CashbackOwner.CategoryShop(
    id = id,
    name = name,
    parent = parent
)