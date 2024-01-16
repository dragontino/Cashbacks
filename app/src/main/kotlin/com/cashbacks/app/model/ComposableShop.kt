package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.Shop

class ComposableShop(
    val id: Long,
    name: String = ""
) {
    constructor(shop: Shop) : this(
        id = shop.id,
        name = shop.name
    )

    var name by mutableStateOf(name)

    fun mapToShop() = Shop(
        id = id,
        name = this.name,
        maxCashback = null
    )
}