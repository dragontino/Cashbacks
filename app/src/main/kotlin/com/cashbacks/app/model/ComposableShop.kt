package com.cashbacks.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cashbacks.domain.model.Shop

class ComposableShop(
    val id: Long = 0,
    private val initialName: String = ""
) {
    constructor(shop: Shop) : this(
        id = shop.id,
        initialName = shop.name
    )

    var name by mutableStateOf(initialName)

    val isChanged get() = name != initialName

    fun mapToShop() = Shop(
        id = id,
        name = this.name,
        maxCashback = null
    )
}