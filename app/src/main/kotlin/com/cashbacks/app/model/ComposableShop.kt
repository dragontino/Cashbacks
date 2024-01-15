package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop

class ComposableShop(
    val id: Long,
    name: String = "",
    cashbacks: List<Cashback> = listOf()
) {
    constructor(shop: Shop) : this(
        id = shop.id,
        name = shop.name,
        cashbacks = shop.cashbacks
    )

    var name by mutableStateOf(name)
    val cashbacks = ComposableList(cashbacks)

    fun mapToShop() = Shop(
        id = id,
        name = this.name,
        cashbacks = this.cashbacks
    )
}